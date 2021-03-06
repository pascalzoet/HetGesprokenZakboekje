package nl.politie.predev.android.zakboek;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
//import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import nl.politie.predev.android.zakboek.model.AccesTokenRequest;
import nl.politie.predev.android.zakboek.model.Multimedia;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PictureActivity extends AppCompatActivity {

	private static final String BASE_HTTPS_URL_DB_API = "https://stempolextras.westeurope.cloudapp.azure.com:8086/";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_picture);
		if(getIntent().getStringExtra(NoteActivity.EXTRA_MESSAGE_NOTE) !=null) {

			String id = getIntent().getStringExtra(NoteActivity.EXTRA_MESSAGE_NOTE);
			getImageContent(id);

		}

	}

	private void getImageContent(String multimediaID) {

		//Base64 content bevat geen padverwijzing naar android
		if(multimediaID.contains("Android")) {
			try{
				setImage(Files.readAllBytes(Paths.get(multimediaID)));
				return;
			}catch (Exception e){
				//TODO
			}
		}

		//Niet uit local storage kunnen halen, dus we halen hem op vanaf de server

		OkHttpClient client = new OkHttpClient();

		String json = "{\"multimediaID\": \"" + multimediaID + "\"}";


		RequestBody body = RequestBody.create(
				MediaType.parse("application/json"), json);

		Request request = new Request.Builder()
				.url(BASE_HTTPS_URL_DB_API + "getmultimedia")
				.post(body)
				.addHeader("Authorization", AccesTokenRequest.accesTokenRequest.getTokenType() + " " + AccesTokenRequest.accesTokenRequest.getAccessToken())
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
//				Log.e("err", e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				final String resp = response.body().string();
				Handler mainHandler = new Handler(getBaseContext().getMainLooper());
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						setImage(resp);
					}
				};
				mainHandler.post(runnable);

			}
		});
	}

	private void setImage(byte[] imageData) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		ImageView iv = findViewById(R.id.show_picture_picture);
		iv.setImageBitmap(bitmap);
	}

	private void setImage(String multimedia){
//		Log.e("dada", multimedia);
		try{
			ObjectMapper om = new ObjectMapper();
			Multimedia multimediaObject = om.readValue(multimedia, Multimedia.class);
			byte[] imageData = Base64.getDecoder().decode(multimediaObject.getContent());
			setImage(imageData);
		} catch (Exception e) {
//			Log.e("asfasf", e.getMessage());
		}
	}
}
