package com.example.servelojasdkexemplo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import br.com.serveloja.servelojasdk.domain.Venda
import br.com.serveloja.servelojasdk.internal.ServelojaTransaction
import br.com.serveloja.servelojasdk.internal.enums.ActionEnum
import br.com.serveloja.servelojasdk.internal.enums.ParcelasEnum
import br.com.serveloja.servelojasdk.internal.enums.TipoTransacaoEnum
import br.com.serveloja.servelojasdk.internal.enums.TransactionEnum
import br.com.serveloja.servelojasdk.internal.interfaces.ServelojaActionCallback
import br.com.serveloja.servelojasdk.internal.providers.TransactionProvider
import kotlinx.android.synthetic.main.activity_transaction.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class TransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        enviarButton.setOnClickListener {
//            sendTransaction()
            confirmaEnvio();
        }
        valorEditText.apply {
            addTextChangedListener(ValorTextWatcher(this))
            setOnClickListener {
                this.setSelection(this.text.length)
            }
            setText("0")
        }
        tipoTransacaoRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                radioCredito.id -> parcelasSpinner.visibility = View.VISIBLE
                radioDebito.id -> parcelasSpinner.visibility = View.GONE
            }
        }
    }

    fun sendTransaction() {
        val valor = valorEditText.text.toString()
        val tipo =
            if (radioCredito.isChecked) TipoTransacaoEnum.CREDITO else TipoTransacaoEnum.DEBITO
        val parcelas =
            if (radioCredito.isChecked) parcelasSpinner.selectedItem.toString().toInt() else 1

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
            useDefaultUI(true)
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
}
