package com.example.servelojasdkexemplo;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ValorTextWatcher implements TextWatcher {

    private EditText editText;

    public ValorTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().matches("^(\\d{1,3}(\\.\\d{3})*|(\\d{1,3})|((\\d{1,3}\\.\\d{3})*) | ((\\d{1,3}(\\.\\d{3})*)*)  )(\\,\\d{2})")
                || (s.toString().charAt(0) == '0' && s.toString().length() > 4)) {

            String userInput = s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

//delete leftZeros
            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0')
                cashAmountBuilder.deleteCharAt(0);
            // complete with 0,0#
            while (cashAmountBuilder.length() < 3) cashAmountBuilder.insert(0, '0');

            cashAmountBuilder.insert(cashAmountBuilder.length() - 2, ',');

            int pointer = cashAmountBuilder.length() - 3;
            int counter = 0;

            while (pointer > 0) {

                if (counter == 3) {
                    cashAmountBuilder.insert(pointer, '.');
                    counter = 0;
                    pointer++;
                }

                if (cashAmountBuilder.charAt(pointer) != '.') {
                    counter++;

                }


                pointer--;
            }


            final String text = cashAmountBuilder.toString();
            editText.setText(text);
            editText.setSelection(editText.getText().length());
        } else
            editText.setSelection(editText.getText().length());

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
