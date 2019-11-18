# Usando a SDK

## Inserindo as credencias para uso do repositório via Jitpack
Abrir o arquivo *gradle.properties* e inserir as seguintes credenciais:

```properties
username=serveloja
authToken=consultar_serveloja
```

## Configurando o projeto para usar o repositório via Jitpack
Acessar o *build.gradle (Project)*, e adicionar o repositório do Jitpack

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

### Configurando metadados e permissões
Abrir o arquivo *AndroidManifest.xml*, e inserir as seguintes permissões

```xml

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
## Iniciando a SDK
Para iniciar a SDK, o método iniciarSdk deverá ser chamado da seguinte forma:
```kotlin
val iniciou = ServelojaSDK.iniciarSdk(aplicationContext, APP_KEY, APP_ID)
```
O método retorna um boolean informando se a SDK foi iniciada com sucesso.

## Definindo pinpad
Para definir a pinpad a ser utilizada, o método definirPinpad deverá ser chamado da seguinte forma:
```kotlin
val definido = ServelojaSDK.definirPinpad(Pinpad(name = device.name, address = device.address))
```
O método retorna um boolean informando se a pinpad foi iniciada com sucesso.

## Realizando uma venda
Para realizar uma venda, o método realizarTransacao deve ser chamado informando os seguintes parâmetros.
```kotlin
fun realizarTransacao(context: Context, transaction: TransactionModel, onFinish: (FinishedTransactionModel, StatusTransacao) -> Unit)
```
Ao finalizar o fluxo de venda, o callback (FinishedTransactionModel, StatusTransacao) -> Unit) será chamado fornecendo o model da transação realizada e seu status


## Regularizar transações
As vezes, ao realizar uma venda algum erro pode impedir que a mesma seja finalizada na Serveloja. Quando isso acontece, a SDK armazena a transação não finalizada para que a mesma possa ser regularizada em um momento posterior.
Para isso, o método regularizarTransacoes deverá ser chamado informando os seguintes parâmetros:
```kotlin 
fun regularizarTransacoes(context: Context, onFinish: (StatusPendencia) -> Unit)
```
Após o método ser executado, o callback fornecido será executado com um model que representa o status das pendências.
