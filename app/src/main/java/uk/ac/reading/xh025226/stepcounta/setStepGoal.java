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

public class SetStepGoal extends AppCompatActivity {
    DatabaseHelper mDatabaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_step_goal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabaseHelper = DatabaseHelper.getInstance(this);
        final TextView textView = (TextView) findViewById(R.id.tv_Step_Goal);
        final EditText editText = (EditText) findViewById(R.id.editText_SG);
        Button submit = (Button) findViewById(R.id.btn_submit_SG);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().equals("")){// Empty string, reject
                    textView.setText("Please input a value");
                }
                else{
                    int value = Integer.parseInt(editText.getText().toString()); // convert input to a int
                    // put the value into the data base
                    mDatabaseHelper.setGoal(value);
                    // feed back: Goal set
                    Intent intent = new Intent(SetStepGoal.this, StepActivity.class);
                    startActivity(intent);

                }

            }
        });
    }

}
