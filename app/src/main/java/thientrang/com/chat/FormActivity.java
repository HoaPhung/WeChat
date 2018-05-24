package thientrang.com.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by THIEN TRANG on 06/05/2018.
 */

public class FormActivity extends AppCompatActivity {
    EditText edt_ques, edt_ans1, edt_ans2, edt_ans3;
    Spinner spinner;
    Button btn_submit;
    ImageView btn_back2;
    ArrayList<String> ans;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_layout);
        init();
        action();

    }

    private void action() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ques, ans1, ans2, ans3, ans4;
                ques = edt_ques.getText().toString();
                ans1 = edt_ans1.getText().toString();
                ans2 = edt_ans2.getText().toString();
                ans3 = edt_ans3.getText().toString();
                if (ques.equals("") || ans1.equals("") || ans2.equals("") || ans3.equals("")){
                    Toast.makeText(FormActivity.this, "You can fill question or answer!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        btn_back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormActivity.super.onBackPressed();
            }
        });
    }

    private void init() {
        edt_ques = (EditText)findViewById(R.id.edt_ques);
        edt_ans1 = (EditText)findViewById(R.id.edt_ans1);
        edt_ans2 = (EditText)findViewById(R.id.edt_ans2);
        edt_ans3 = (EditText)findViewById(R.id.edt_ans3);
        spinner = (Spinner)findViewById(R.id.spinner);
        btn_submit = findViewById(R.id.btn_submit);
        btn_back2 = (ImageView) findViewById(R.id.btn_back2);
        ans = new ArrayList<>();
        ans.add("A");
        ans.add("B");
        ans.add("C");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,ans);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }


}
