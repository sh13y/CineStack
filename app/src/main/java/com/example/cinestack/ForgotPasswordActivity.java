package com.example.cinestack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * ForgotPasswordActivity - Handles password reset via security question verification
 * Three-step flow: 1) Enter username/email  2) Answer security question  3) Set new password
 *
 * @author ICT3214 Group Project
 * @version 1.0
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    // Step layouts
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;

    // Step 1
    private TextInputLayout tilUsernameOrEmail;
    private TextInputEditText etUsernameOrEmail;
    private MaterialButton btnNext1;

    // Step 2
    private TextView tvSecurityQuestion;
    private TextInputLayout tilSecurityAnswer;
    private TextInputEditText etSecurityAnswer;
    private MaterialButton btnVerifyAnswer;

    // Step 3
    private TextInputLayout tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText etNewPassword, etConfirmNewPassword;
    private MaterialButton btnResetPassword;

    // Common
    private TextView tvSubtitle, tvBackToLogin;

    // Data
    private DatabaseHelper databaseHelper;
    private String usernameOrEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        // Step layouts
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        // Step 1
        tilUsernameOrEmail = findViewById(R.id.tilUsernameOrEmail);
        etUsernameOrEmail = findViewById(R.id.etUsernameOrEmail);
        btnNext1 = findViewById(R.id.btnNext1);

        // Step 2
        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);
        tilSecurityAnswer = findViewById(R.id.tilSecurityAnswer);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnVerifyAnswer = findViewById(R.id.btnVerifyAnswer);

        // Step 3
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Common
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setClickListeners() {
        // Step 1 - Find user
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStep1();
            }
        });

        // Step 2 - Verify security answer
        btnVerifyAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStep2();
            }
        });

        // Step 3 - Reset password
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStep3();
            }
        });

        // Back to login link
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    /**
     * Step 1: Verify user exists and retrieve security question
     */
    private void handleStep1() {
        tilUsernameOrEmail.setError(null);

        usernameOrEmail = etUsernameOrEmail.getText().toString().trim();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            tilUsernameOrEmail.setError("Username or email is required");
            etUsernameOrEmail.requestFocus();
            return;
        }

        // Check if user exists
        if (!databaseHelper.checkUserExists(usernameOrEmail)) {
            tilUsernameOrEmail.setError("No account found with this username or email");
            etUsernameOrEmail.requestFocus();
            return;
        }

        // Get security question
        String question = databaseHelper.getSecurityQuestion(usernameOrEmail);
        if (question == null) {
            Toast.makeText(this, "Unable to retrieve security question. Please contact support.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Switch to step 2
        tvSecurityQuestion.setText(question);
        tvSubtitle.setText("Answer the security question below");
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        etSecurityAnswer.requestFocus();
    }

    /**
     * Step 2: Verify the security answer
     */
    private void handleStep2() {
        tilSecurityAnswer.setError(null);

        String answer = etSecurityAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(answer)) {
            tilSecurityAnswer.setError("Please enter your answer");
            etSecurityAnswer.requestFocus();
            return;
        }

        // Verify the answer
        if (!databaseHelper.verifySecurityAnswer(usernameOrEmail, answer)) {
            tilSecurityAnswer.setError("Incorrect answer. Please try again.");
            etSecurityAnswer.requestFocus();
            return;
        }

        // Switch to step 3
        tvSubtitle.setText("Create a new password for your account");
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.VISIBLE);
        etNewPassword.requestFocus();
    }

    /**
     * Step 3: Reset the password
     */
    private void handleStep3() {
        tilNewPassword.setError(null);
        tilConfirmNewPassword.setError(null);

        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmNewPassword.getText().toString();

        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("Password is required");
            etNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            tilNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmNewPassword.setError("Please confirm your password");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        // Update password in database
        boolean updated = databaseHelper.updatePassword(usernameOrEmail, newPassword);

        if (updated) {
            Toast.makeText(this, "Password reset successful! Please sign in.",
                    Toast.LENGTH_LONG).show();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Failed to reset password. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate back to login screen
     */
    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // If on step 2 or 3, go back to previous step
        if (layoutStep3.getVisibility() == View.VISIBLE) {
            layoutStep3.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
            tvSubtitle.setText("Answer the security question below");
        } else if (layoutStep2.getVisibility() == View.VISIBLE) {
            layoutStep2.setVisibility(View.GONE);
            layoutStep1.setVisibility(View.VISIBLE);
            tvSubtitle.setText("Enter your username or email to continue");
        } else {
            super.onBackPressed();
        }
    }
}
