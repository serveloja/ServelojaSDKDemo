package com.example.servelojasdkexemplo

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import br.com.serveloja.servelojasdk.internal.database.PinpadObj
import br.com.serveloja.servelojasdk.internal.interfaces.ServelojaCallback
import br.com.serveloja.servelojasdk.internal.providers.BluetoothProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

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

