package com.example.mobilelogbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private MaterialAutoCompleteTextView mauctv_property;
    private MaterialAutoCompleteTextView mauctv_bedroom;
    private TextInputEditText tiete_date_time;
    private ArrayList<String> property_arr;
    private ArrayList<String> bedrooms_arr;
    private ArrayAdapter<String> array_adapter;
    private MaterialButton m_btn_submit;
    private TextInputEditText tiete_price;
    private TextInputEditText tiete_note;
    private TextInputEditText tiete_name;
    private MaterialRadioButton m_radio_btn_furnished;
    private MaterialRadioButton m_radio_btn_unfurnished;
    private MaterialRadioButton m_radio_btn_part_furnished;
    private RadioGroup rg_furniture;

    int date, month, year, hour, minute;
    int m_date, m_month, m_year, m_hour, m_minute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hook();
        disableTouchFocus();
        setDropdownListProperty();
        setDropdownListBedrooms();
        dateTimePicker();
        validateInfoSubmit();
        textChangeWatcher();
        


    }

    private void validateInfoSubmit() {
        m_btn_submit.setOnClickListener(view -> {
            String property = Objects.requireNonNull(mauctv_property.getText()).toString().trim();
            String bedroom = Objects.requireNonNull(mauctv_property.getText()).toString().trim();
            String dateTime = Objects.requireNonNull(tiete_date_time.getText()).toString().trim();
            String price = Objects.requireNonNull(tiete_price.getText()).toString().trim();
            String name = Objects.requireNonNull(tiete_name.getText()).toString().trim();
            String note = Objects.requireNonNull(tiete_note.getText()).toString().trim();
            String furniture = Objects.requireNonNull(validateFurnitureStatus()).trim();
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (property.isEmpty()) {
                TextInputLayout layout = findViewById(R.id.til_property);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }

            if (bedroom.isEmpty()) {
                TextInputLayout layout = findViewById(R.id.til_bedrooms);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }

            if (dateTime.isEmpty()) {
                TextInputLayout layout = findViewById(R.id.til_date_time);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }

            if (price.isEmpty()) {
                TextInputLayout layout = findViewById(R.id.til_monthlyPrice);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }

            if (name.isEmpty()) {
                TextInputLayout layout = findViewById(R.id.til_name);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }


            if (TextUtils.isEmpty(property) || TextUtils.isEmpty(bedroom) || TextUtils.isEmpty(dateTime) || TextUtils.isEmpty(price) || TextUtils.isEmpty(name)) {
                Toast.makeText(MainActivity.this, getString(R.string.validate_empty_fields), Toast.LENGTH_SHORT).show();


                vibrator.vibrate(400);
            } else {
                submitInfo(property, bedroom, dateTime, furniture, price, note, name);
            }
        });
    }


    private void submitInfo(String property, String bedroom, String dateTime, String furniture, String price, String note, String name) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this,R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        builder.setMessage(getString(R.string.message_alert_dialog_txt));
        builder.setCancelable(true);

        builder.setPositiveButton(getString(R.string.positive_button), (dialogInterface, i) -> {
//                Firebase connect

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> rentalInfo = new HashMap<>();
                rentalInfo.put("propertyType",property);
                rentalInfo.put("bedroom",bedroom);
                rentalInfo.put("dateTime",dateTime);
                rentalInfo.put("furnitureType",furniture);
                rentalInfo.put("monthlyPrice",price);
                rentalInfo.put("note",note);
                rentalInfo.put("nameOfReporter",name);
            db.collection("rental").add(rentalInfo).addOnSuccessListener(documentReference -> {
                Toast.makeText(MainActivity.this,getString(R.string.successful_submit_toast_txt),Toast.LENGTH_SHORT).show();
                dialogInterface.cancel();
                clearText();
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,getString(R.string.failure_submit_toast_txt),Toast.LENGTH_SHORT).show();
                    dialogInterface.cancel();
                }
            });

        });
        builder.setNegativeButton(getString(R.string.negative_button), (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void clearText() {
        mauctv_property.setText("");
        mauctv_bedroom.setText("");
        tiete_date_time.setText("");
        tiete_price.setText("");
        tiete_note.setText("");
        tiete_name.setText("");
        rg_furniture.clearCheck();

    }

    private void textChangeWatcher() {
        mauctv_property.addTextChangedListener(tcw_property);
        mauctv_bedroom.addTextChangedListener(tcw_bedroom);
        tiete_date_time.addTextChangedListener(tcw_dateTime);
        tiete_price.addTextChangedListener(tcw_price);
        tiete_name.addTextChangedListener(tcw_name);
    }

    private final TextWatcher tcw_property = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_property);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher tcw_bedroom = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_bedrooms);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher tcw_dateTime = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_date_time);
            if(editable.length() == 1){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher tcw_price = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_monthlyPrice);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher tcw_name = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            TextInputLayout layout = findViewById(R.id.til_name);
            if(editable.length() == 0){
                layout.setErrorEnabled(true);
                layout.setError(getString(R.string.validate_information_error));
                layout.requestFocus();
            }
            else{
                layout.setErrorEnabled(false);
            }
        }
    };

    private String validateFurnitureStatus(){
        String f_status="";
        if(m_radio_btn_furnished.isChecked()){
            f_status = getString(R.string.rb_furnished);
        }
        if (m_radio_btn_unfurnished.isChecked()){
            f_status = getString(R.string.rb_part_furnished);
        }
        if(m_radio_btn_part_furnished.isChecked()){
            f_status = getString(R.string.rb_part_furnished);
        }
        return f_status;
    }






    @SuppressLint("ClickableViewAccessibility")
    private void dateTimePicker() {
        tiete_date_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                date = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, MainActivity.this,year, month,date);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
}
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        m_year = year;
        m_date = date;
        m_month = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, MainActivity.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        m_hour = i;
        m_minute = i1;


        tiete_date_time.setText(m_year+"-" + validateMonth() +"-"+m_date+ " " + m_hour + ":" +validateMin());
    }

    private String validateMin(){
        String n_min;
        if(m_minute < 10){
            n_min= "0"+ m_minute;
        }else{
            n_min = String.valueOf(m_minute);
        }
        return n_min;
    }


    private String validateMonth() {
        String v_month;
        switch (m_month){
            case 1:
                v_month ="JAN";
                break;
            case 2:
                v_month = "FEB";
                break;
            case 3:
                v_month = "MAR";
                break;
            case 4:
                v_month = "APR";
                break;
            case 5:
                v_month = "MAY";
                break;
            case 6:
                v_month = "JUN";
                break;
            case 7:
                v_month = "JUL";
                break;
            case 8:
                v_month = "AUG";
                break;
            case 9:
                v_month = "SEP";
                break;
            case 10:
                v_month = "OTC";
                break;
            case 11:
                v_month = "NOV";
                break;
            case 12:
                v_month = "DEC";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + m_month);
        }
        return v_month;
    }

    private void setDropdownListBedrooms() {
        array_adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.item_dropdown_list,property_arr);
        mauctv_property.setAdapter(array_adapter);
        mauctv_property.setThreshold(1);
    }

    private void setDropdownListProperty() {
        array_adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.item_dropdown_list,bedrooms_arr);
        mauctv_bedroom.setAdapter(array_adapter);
        mauctv_bedroom.setThreshold(1);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void disableTouchFocus() {
        tiete_date_time.setOnTouchListener((view, motionEvent) -> {
            int inType = tiete_date_time.getInputType();
            tiete_date_time.setInputType(InputType.TYPE_NULL);
            tiete_date_time.onTouchEvent(motionEvent);
            tiete_date_time.setInputType(inType);
            return true;
        });

        mauctv_property.setOnTouchListener((view, motionEvent) -> {
            int inType = mauctv_property.getInputType();
            mauctv_property.setInputType(InputType.TYPE_NULL);
            mauctv_property.onTouchEvent(motionEvent);
            mauctv_property.setInputType(inType);
            return true;
        });

        mauctv_bedroom.setOnTouchListener((view, motionEvent) -> {
            int inType = mauctv_bedroom.getInputType();
            mauctv_bedroom.setInputType(InputType.TYPE_NULL);
            mauctv_bedroom.onTouchEvent(motionEvent);
            mauctv_bedroom.setInputType(inType);
            return true;
        });
    }

    private void hook() {
        mauctv_property = findViewById(R.id.mactxt_property);
        mauctv_bedroom = findViewById(R.id.mactxt_bedroom);
        tiete_date_time = findViewById(R.id.edt_date_time);
        String[] arr_property = {"Apartment","House","Flat","Bungalow"};
        property_arr = new ArrayList<>(Arrays.asList(arr_property));
        mauctv_bedroom = findViewById(R.id.mactxt_bedroom);
        bedrooms_arr = new ArrayList<>();
        bedrooms_arr.add("Studio");
        for(int i = 1; i <= 20; i++){
            if(i < 10){
                bedrooms_arr.add("0"+ i);
            }
            else{
                bedrooms_arr.add(String.valueOf(i));
            }
        }
        m_radio_btn_furnished = findViewById(R.id.radio_btn_furnished);
        m_radio_btn_unfurnished = findViewById(R.id.radio_btn_unfurnished);
        m_radio_btn_part_furnished = findViewById(R.id.radio_btn_part);
        tiete_price = findViewById(R.id.edt_monthlyPrice);
        tiete_note = findViewById(R.id.edt_note);
        tiete_name = findViewById(R.id.edt_name);
        m_btn_submit = findViewById(R.id.mbtn_submit);
        rg_furniture = findViewById(R.id.radio_group);
    }

}