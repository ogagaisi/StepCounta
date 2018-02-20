package uk.ac.reading.xh025226.stepcounta;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by ogaga isiavwe on 04-Dec-17.
 */
public class GraphActivity extends AppCompatActivity{
    LineGraphSeries<DataPoint> series; // represents the data points
    double x,y;
    DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        x = 0; // x represents the number of steps, the minimum number of steps allowed is zero
        y = 0;
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        mDatabaseHelper = new DatabaseHelper(this);

        if(mDatabaseHelper.isEmpty()){// Display an empty graph to the user and a message
            series.appendData(new DataPoint(x,y), true, 24); // the last value represents the amount of data points
            toastMessage("looks like no steps have been recorded, maybe have a nice stroll?");
        }
        else{// get the things needed to plot the graph

            Cursor data = mDatabaseHelper.getData(); // returns all the rows in the database

            while(data.moveToNext()){// while we can still move to the next row
                //get the value from the database in colum 1
                //then add it to the x axis
                x = x + 1;
                y = data.getInt(1);
                series.appendData(new DataPoint(x, y), true, mDatabaseHelper.getNoOfRows()); // the last value represents the amount of data points
            }
        }


        graph.addSeries(series);
    }
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
