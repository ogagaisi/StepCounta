package uk.ac.reading.xh025226.stepcounta;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputActivity extends AppCompatActivity {
    DatabaseHelper mDatabaseHelper;

    TextView tvMessage;
    EditText eText;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input);
        mDatabaseHelper = DatabaseHelper.getInstance(this);
        tvMessage = (TextView) findViewById(R.id.tv_Message);
        eText = (EditText) findViewById(R.id.editText);
        submit = (Button) findViewById(R.id.btn_submit);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
             public void onClick(View arg0){
                double value = Double.parseDouble(eText.getText().toString()); // convert input to a double
                // value is too small
                if(value < 54.61) { //the recorded shortest man in world
                    tvMessage.setText("The height you entered was too small, Please input another value");
                }
                else{
                    mDatabaseHelper.addHeigth(value);
                    Intent intent = new Intent(InputActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }

        });
    }

}
