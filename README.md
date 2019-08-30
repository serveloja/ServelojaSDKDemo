# Metadados
**Alguns metadados devem ser adicionados a seu manifest:**
- **br.com.serveloja.sdk.ApplicationKey**
	Sua chave de aplicação a ser utilizada pela SDK

- **br.com.serveloja.sdk.ApplicationName**
	Nome da sua aplicação. Utilizada pela SDK da stone para identificar suas transações

- **br.com.serveloja.sdk.EnvironmentMode**
	Modo de operação da SDK. Aceita os valores SANDBOX e PRODUCTION
	Acesse a [documentação](https://sdkandroid.stone.com.br/reference#preparando-aplicacao) da stone para mais detalhes

# Providers
### BluetoothProvider
- Provider para conexão com pinpads
- Recebe um objeto PinpadObj e Context em sua instancialização
- Possivel setar um ServelojaCallback para receber os eventos de conexão
- Use BluetoothProvider.getListOfErrors para obter uma lista de ErrorsEnum

### DisplayMsgProvider
- Provider para escrever mensagens no display da pinpad conectada
- Recebe um Context e a String a ser exibida na pinpad

### TablesProvider
- Provider para atualizar as tabelas
- Recebe um Context e a transação a ser atualizada

### TransactionProvider
- Provider para realizar transações
- Recebe um Context e um ServelojaTransaction que representa a transação a ser realizada
- Possivel setar um ServelojaCallback para receber os eventos da transação
- Use TransactionProvider.getListOfErrors para obter a lista de erros da to provider
- Use TransactionProvider.getTransactionStatus() para obter o status da transação

# Usando a SDK
## Inserindo as credencias para uso do repositório via Jitpack
Abrir o arquivo *gradle.properties* e inserir as seguintes credenciais:

```properties
username=serveloja
authToken=jp_h2ko7hdreu2pk4r2edistfmkrt
```

## Configurando o projeto para usar o repositório via Jitpack
Aceesar o *build.gradle (Project)*, e adicionar o repositório do Jitpack

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
}
```
## Inserindo referências no gradle

```gradle
ext {
    koin_version = '1.0.1'
    stone_version = '3.0.5'
    retrofit_version = '2.4.0'
    okhttp_version = '3.11.0'
    coroutines_version = '1.0.0'
}

dependencies {
    ...

    // koin
    implementation "org.koin:koin-android:$koin_version"

    // stone
    implementation "br.com.stone:stone-sdk:$stone_version"

    // retrofit and OkHttp
    implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
    implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
    implementation "com.squareup.okhttp3:logging-interceptor:$okhttp_version"

    // coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
    implementation 'com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2'

    // serveloja
    implementation 'com.github.serveloja:ServelojaSDK:Tag'
}
```

### Configurando metadados e permissões
Abrir o arquivo *AndroidManifest.xml*, e inserir os seguintes parâmetros

```xml

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<application ... >

    <!-- Chave da aplicação -->
    <meta-data android:name="br.com.serveloja.sdk.ApplicationKey" android:value="SANDBOX" />

    <!-- Nome da aplicação -->
    <meta-data android:name="br.com.serveloja.sdk.ApplicationName" android:value="SANDBOX" />

    <!--
        Modo de operação
        Define qual o modo de operação da SDK
        Valores permitidos: SANDBOX e PRODUCTION
        SANDBOX -> Modo de testes da SDK
        PRODUCTION -> Modo de Produção da SDK
    -->
    <meta-data android:name="br.com.serveloja.sdk.EnvironmentMode" android:value="SANDBOX" />

 </application>
```

## Listando os dispositivos bluetooth e realizando pareamento
```kotlin
class MainActivity : AppCompatActivity() {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        turnBluetoothOn()
        listBluetoothDevices()
    }

    private fun turnBluetoothOn() {
        do {
            bluetoothAdapter.enable()
        } while (!bluetoothAdapter.isEnabled)
    }

    private fun listBluetoothDevices() {
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1)

        // lista de dispositivos pareados
        val paired = bluetoothAdapter.bondedDevices

        if (paired.isNotEmpty()) {
            for (device in paired) {
                arrayAdapter.add("${device.name}_${device.address}")
            }
        }

        devicesList.adapter = arrayAdapter

        // selecionando o dispositivo e realizando pareamento
        devicesList.setOnItemClickListener { parent, view, position, id ->
            val pinpad = paired.elementAt(position)
            val pinpadSelected = PinpadObj(pinpad.name, pinpad.address, false)  /* Dispositivo bluetooth a ser conectado */
            val btProvider = BluetoothProvider(this@MainActivity, pinpadSelected)   /* Provider de conexão com o dispositivo selecionado */
            btProvider.setDialogMessage("Criando conexão com o pinpad selecionado")
            btProvider.useDefaultUI(false)
            btProvider.setConnectionCallback(object : ServelojaCallback {
                override fun onSuccess() {
                    toast("Pinpad conectado")
                    startActivity<TransactionActivity>()
                }

                override fun onError() {
                    toast("Erro durante a conexão")
                }
            })
            btProvider.execute()
        }
    }
}
```

## Realizando uma venda
```kotlin
fun sendTransaction() {

    val valor = valorEditText.text.toString()
    val tipo = if (radioCredito.isChecked) TipoTransacaoEnum.CREDITO else TipoTransacaoEnum.DEBITO
    val parcelas = if (radioCredito.isChecked) parcelasSpinner.selectedItem.toString().toInt() else 1

    /* Model que representa a venda a ser realizada */
    val venda = Venda(
        tipo,
        valor,
        ParcelasEnum.from(parcelas),
        descricao = "",
        cpfCnpjComprador = "12312312387",
        latLong = "",
        nomeTitular = "TESTE TESTE",
        ddd = "79",
        telefone = "999999999"
    )

    /* Model que envelopa a transação a ser realizada
    * Recebe um model de Venda em sua instacialização */
    val transaction = ServelojaTransaction(venda)

    /*
    * Provider de Transação
    * Recebe um Context e ServelojaTransaction em sua instacialização
    * Deve ser setado o callback antes de ser executado
    * É possivel obter o status e a lista de erros do evento de transação a partir dele
    * */
    val provider = TransactionProvider(this@TransactionActivity, transaction).apply {
        useDefaultUI(false)
        setDialogMessage("Enviando...")
        setDialogTitle("Aguarde")

        /*
        * Callback do provider da transação
        * */
        setConnectionCallback(object : ServelojaActionCallback {
            override fun onStatusChanged(actionEnum: ActionEnum) {
                Log.d("STATUS_TRANSAÇÂO", actionEnum.name)
            }

            override fun onSuccess() {
                if (getTransactionStatus() == TransactionEnum.APROVADO) {
                    toast("Transação aprovada")
                } else {
                    toast("Erro na transação: ${getMenssageFromAuthorize()}")
                }
            }

            override fun onError(message: String) {
                toast(message)
            }
        })
        execute(venda)
    }
}

fun confirmaEnvio() {
    alert {
        message = "Confirma esta solicitação"
        positiveButton("Sim") { sendTransaction() }
        negativeButton("Não") { null }
    }.show()
}
```
