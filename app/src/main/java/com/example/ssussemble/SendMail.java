package com.example.ssussemble;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class SendMail extends AppCompatActivity {
    String user = "tmdals078@gmail.com"; // 보내는 계정의 id
    String password = "slip ozsk sizw tcmm"; // 보내는 계정의 pwd

    MailSender gMailSender = new MailSender(user, password);
    String emailCode = gMailSender.getEmailCode();
    public void sendSecurityCode(Context context, String sendTo) {
        new Thread(() -> {
            try {
                Log.d("sendSecurityCode", "Sending email to: " + sendTo);
                gMailSender.sendMail("인증번호 발송", "인증번호: " + emailCode, sendTo);
                runOnUiThread(() -> Toast.makeText(context, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show());
            } catch (SendFailedException e) {
                runOnUiThread(() -> Toast.makeText(context, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show());
                Log.e("sendSecurityCode", "Invalid email format", e);
            } catch (MessagingException e) {
                runOnUiThread(() -> Toast.makeText(context, "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show());
                Log.e("sendSecurityCode", "Messaging error", e);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(context, "인증번호 전송 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                Log.e("sendSecurityCode", "Unknown error", e);
            }
        }).start();
    }
}