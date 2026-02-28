package com.example.medisageapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView chatRecycler;

    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    private ChatAdapter adapter;

    // 🔐 PUT YOUR HUGGING FACE TOKEN HERE


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatRecycler = findViewById(R.id.chatRecycler);

        adapter = new ChatAdapter(chatList);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String userMsg = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(userMsg)) {
            Toast.makeText(this, "Type a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // ➤ Show user message
        chatList.add(new ChatMessage(userMsg, true));
        adapter.notifyItemInserted(chatList.size() - 1);
        chatRecycler.scrollToPosition(chatList.size() - 1);

        etMessage.setText("");

        // ➤ Call Hugging Face API
        callHuggingFace(userMsg);
    }

    private void callHuggingFace(String prompt) {
        new Thread(() -> {
            try {
                // ✅ Keep this URL - it is the correct 2026 Router endpoint
                URL url = new URL("https://router.huggingface.co/v1/chat/completions");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN.trim());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 📝 Updated JSON with a guaranteed Chat-Compatible model
                JSONObject json = new JSONObject();
                // Qwen 2.5 is highly stable for the Router's chat endpoint
                json.put("model", "Qwen/Qwen2.5-7B-Instruct");

                JSONArray messages = new JSONArray();
                JSONObject messageObj = new JSONObject();
                messageObj.put("role", "user");
                messageObj.put("content", prompt);
                messages.put(messageObj);

                json.put("messages", messages);
                json.put("max_tokens", 500);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.close();

                int code = conn.getResponseCode();
                if (code == 200) {
                    String response = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A").next();
                    JSONObject responseJson = new JSONObject(response);

                    // Parsing logic remains the same for OpenAI-style responses
                    String aiReply = responseJson.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> {
                        chatList.add(new ChatMessage(aiReply.trim(), false));
                        adapter.notifyItemInserted(chatList.size() - 1);
                        chatRecycler.scrollToPosition(chatList.size() - 1);
                    });
                } else {
                    String error = new java.util.Scanner(conn.getErrorStream()).useDelimiter("\\A").next();
                    Log.e("HF_DEBUG", "Router Error: " + error);
                    showError("Router Error " + code + ": " + error);
                }
            } catch (Exception e) {
                Log.e("HF_DEBUG", "Crash", e);
                showError("Connection Failed");
            }
        }).start();
    }

    private void showError(String msg) {
        runOnUiThread(() ->
                Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show()
        );
    }
}
