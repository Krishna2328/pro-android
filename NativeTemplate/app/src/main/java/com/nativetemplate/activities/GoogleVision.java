package com.nativetemplate.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.nativetemplate.utils.DividerItemDecoration;
import com.nativetemplate.utils.PackageManagerUtils;
import com.nativetemplate.utils.PermissionUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GoogleVision extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCeYbQfnIOKvq1sRurIYTzpk7NxDeoT-FM";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = GoogleVision.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    boolean Selected_Camera=false,Selected_Gallery=false;


    ArrayList<HashMap<String,String>> match_images,match_pages,partial_images,web_entities;
    HashMap<String,String> matchImages_map,matchPages_map,partialImages_map,webEntities_map;
    String strResponses="";

    RelativeLayout srl_match_images;
    ImageView siv_image;
    RecyclerView srecycler_match_images,srecycler_partialMatchingImages,srecycler_pagesWithMatchingImages,srecycler_webEntities;

    TextView srl_welcome,stv_pagesWithMatchingImages,stv_visuallySimilarImages,stv_partialMatchingImages,stv_webEntities;
    HomeAdapter adapter,adapter1;
    ListAdapter listAdapter;
    ListAdapterEntities listEntities;
    LinearLayoutManager srecycler_match_images_lay,srecycler_partialMatchingImages_lay,srecycler_pagesWithMatchingImages_lay;

    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_vision);

       getSupportActionBar().setTitle("Google Vision");
        srl_welcome=(TextView)findViewById(R.id.xrl_welcome);
        stv_pagesWithMatchingImages=(TextView)findViewById(R.id.xtv_pagesWithMatchingImages);
        stv_visuallySimilarImages=(TextView)findViewById(R.id.xtv_visuallySimilarImages);
        stv_partialMatchingImages=(TextView)findViewById(R.id.xtv_partialMatchingImages);
        stv_webEntities=(TextView)findViewById(R.id.xtv_webEntities);
        srl_match_images=(RelativeLayout)findViewById(R.id.xrl_match_images);
        siv_image=(ImageView)findViewById(R.id.xiv_image);
        srecycler_match_images=(RecyclerView)findViewById(R.id.xrecycler_match_images);
        srecycler_pagesWithMatchingImages=(RecyclerView)findViewById(R.id.xrecycler_pagesWithMatchingImages);
        srecycler_webEntities=(RecyclerView)findViewById(R.id.xrecycler_webEntities);
       srecycler_partialMatchingImages=(RecyclerView)findViewById(R.id.xrecycler_partialMatchingImages);

        progressBar = ProgressDialog.show(this,"", "Please wait....");
        progressBar.hide();


        srecycler_match_images_lay = new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        srecycler_match_images_lay.setOrientation(LinearLayoutManager.HORIZONTAL);
        srecycler_match_images.setLayoutManager(srecycler_match_images_lay);
       srecycler_match_images.addItemDecoration(new DividerItemDecoration(7));

        srecycler_partialMatchingImages_lay = new LinearLayoutManager(this);
        srecycler_partialMatchingImages_lay.setOrientation(LinearLayoutManager.HORIZONTAL);
        srecycler_partialMatchingImages.setLayoutManager(srecycler_partialMatchingImages_lay);
        srecycler_partialMatchingImages.addItemDecoration(new DividerItemDecoration(7));

        srecycler_pagesWithMatchingImages_lay = new LinearLayoutManager(getApplicationContext());
        srecycler_pagesWithMatchingImages.setLayoutManager(srecycler_pagesWithMatchingImages_lay);
        srecycler_pagesWithMatchingImages.addItemDecoration(new DividerItemDecoration(7));

        RecyclerView.LayoutManager srecycler_webEntities_lay = new GridLayoutManager(getApplication(), 2);
        srecycler_webEntities.setLayoutManager(srecycler_webEntities_lay);
        srecycler_webEntities.addItemDecoration(new DividerItemDecoration(7));

        match_images=new ArrayList<>();
        match_pages=new ArrayList<>();
        partial_images=new ArrayList<>();
        web_entities=new ArrayList<>();

    }

    public void startGalleryChooser()
    {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera()
    {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA))
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile()
    {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Selected_Gallery=true;
            uploadImage(data.getData());
        }
        else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK)
        {
            Selected_Camera=true;
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                        1200);

                callCloudVision(bitmap);
                if(bitmap!=null)
                {
                   siv_image.setVisibility(View.VISIBLE);
                    srl_welcome.setVisibility(View.GONE);
                    siv_image.setImageBitmap(bitmap);
                }


            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);
        progressBar.show();
        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            //labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setType("WEB_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result)
            {
                /*mImageDetails.setOnTouchListener(new LinkMovementMethodOverride());
                mImageDetails.setText(result);*/
                progressBar.hide();
                if(match_images.size()!=0)
                {
                    srl_welcome.setVisibility(View.GONE);
                    srl_match_images.setVisibility(View.VISIBLE);

                    adapter = new HomeAdapter(match_images);
                    srecycler_match_images.setItemAnimator(new DefaultItemAnimator());
                    srecycler_match_images.setAdapter(adapter);
                    Log.e("visuallySimilarImages"+match_images.size(),match_images.toString());

                }
                else
                {
                    Log.e("visuallySimilarImages","kll");
                    srl_welcome.setVisibility(View.VISIBLE);
                    srl_match_images.setVisibility(View.GONE);
                    stv_visuallySimilarImages.setVisibility(View.GONE);
                    srecycler_match_images.setVisibility(View.GONE);
                }

                if(match_pages.size()!=0)
                {
                    srl_welcome.setVisibility(View.GONE);
                    srl_match_images.setVisibility(View.VISIBLE);
                    listAdapter = new ListAdapter(match_pages);
                    srecycler_pagesWithMatchingImages.setItemAnimator(new DefaultItemAnimator());
                    srecycler_pagesWithMatchingImages.setAdapter(listAdapter);
                    Log.e("pagesWithMatchingImages"+match_images.size(),match_images.toString());

                }
                else
                {
                   stv_pagesWithMatchingImages.setVisibility(View.GONE);
                    srecycler_pagesWithMatchingImages.setVisibility(View.GONE);
                }

                if(partial_images.size()!=0)
                {
                    adapter1 = new HomeAdapter(partial_images);
                    srecycler_partialMatchingImages.setItemAnimator(new DefaultItemAnimator());
                    srecycler_partialMatchingImages.setAdapter(adapter1);

                    srl_welcome.setVisibility(View.GONE);
                    srl_match_images.setVisibility(View.VISIBLE);
                }
                else
                {
                    /*srl_welcome.setVisibility(View.VISIBLE);
                    srl_match_images.setVisibility(View.GONE);*/
                    stv_partialMatchingImages.setVisibility(View.GONE);
                    srecycler_partialMatchingImages.setVisibility(View.GONE);
                }

                if(web_entities.size()!=0)
                {
                    srl_welcome.setVisibility(View.GONE);
                    srl_match_images.setVisibility(View.VISIBLE);
                    listEntities = new ListAdapterEntities(web_entities);
                    srecycler_webEntities.setItemAnimator(new DefaultItemAnimator());
                    srecycler_webEntities.setAdapter(listEntities);

                }
                else
                {
                   stv_webEntities.setVisibility(View.GONE);
                    srecycler_webEntities.setVisibility(View.GONE);
                }

            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response)
    {

        Log.e("Response",response.toString());
        String message = "I found these things:\n\n";

       /* List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%s: %s", label.getScore(), label.getDescription());
                Log.e("MEssage",message);
                message += "\n";
            }
        } else
        {
            message += "nothing";
        }*/
        try
        {
            JSONObject jsonObject=new JSONObject(response);
            Log.e("Response",jsonObject.toString());
            strResponses=jsonObject.getString("responses");
            JSONArray jsonarr=new JSONArray(strResponses);

            JSONObject webjson=jsonarr.getJSONObject(0);
            Log.e("Response",webjson.toString());
            if(webjson.has("webDetection"))
            {
                String webDetection=webjson.getString("webDetection");
                JSONObject web1=new JSONObject(webDetection);
                if(web1.has("pagesWithMatchingImages"))
                {
                    String web=web1.getString("pagesWithMatchingImages");
                    JSONArray webarr=new JSONArray(web);

                    for(int i=0;i<webarr.length();i++)
                    {
                        JSONObject obj=webarr.getJSONObject(i);
                        String URL=obj.getString("url");
                        Log.e("URL--"+i,"---"+URL);

                        matchPages_map=new HashMap<>();
                        matchPages_map.put("url",URL);

                        match_pages.add(matchPages_map);

                        message +=URL;
                        message += "\n\n";
                    }

                    Log.e("pagesWithMatchingImages"+match_pages.size(),match_pages.toString());
                }
                 if(web1.has("visuallySimilarImages"))
                {
                    String web=web1.getString("visuallySimilarImages");
                    JSONArray webarr=new JSONArray(web);

                    for(int i=0;i<webarr.length();i++)
                    {
                        JSONObject obj=webarr.getJSONObject(i);
                        String URL=obj.getString("url");
                        Log.e("URL--"+i,"---"+URL);

                        matchImages_map=new HashMap<>();
                        matchImages_map.put("url",URL);

                        match_images.add(matchImages_map);

                        message +=URL;
                        message += "\n\n";
                    }

                    Log.e("visuallySimilarImages"+match_images.size(),match_images.toString());

                }
                if(web1.has("fullMatchingImages"))
                {
                    String web=web1.getString("fullMatchingImages");
                    JSONArray webarr=new JSONArray(web);

                    for(int i=0;i<webarr.length();i++)
                    {
                        JSONObject obj=webarr.getJSONObject(i);
                        String URL=obj.getString("url");
                        Log.e("URL--"+i,"---"+URL);

                       partialImages_map=new HashMap<>();
                        partialImages_map.put("url",URL);

                        partial_images.add(partialImages_map);

                        message +=URL;
                        message += "\n\n";
                    }

                    Log.e("partialMatchingImages"+partial_images.size(),partial_images.toString());

                }

                if(web1.has("webEntities"))
                {
                    String web=web1.getString("webEntities");
                    JSONArray webarr=new JSONArray(web);

                    for(int i=0;i<webarr.length();i++)
                    {
                        JSONObject obj=webarr.getJSONObject(i);
                        String score="0.00",desc="Nothing";
                        if(obj.has("score"))
                        score=obj.getString("score");

                        if(obj.has("description"))
                        desc=obj.getString("description");


                        webEntities_map=new HashMap<>();
                        webEntities_map.put("score",score);
                        webEntities_map.put("description",desc);

                        web_entities.add(webEntities_map);

                        message +=score;
                        message += "\n\n";
                    }

                    Log.e("webEntities"+partial_images.size(),partial_images.toString());
                }



            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }



    public class HomeAdapter extends RecyclerView.Adapter<MyViewHolder>
    {
        private ArrayList<HashMap<String,String>> button_list = new ArrayList<>();

        public HomeAdapter(ArrayList<HashMap<String,String>> button_list) {
            this.button_list=button_list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_image, parent, false);
            MyViewHolder rcv = new MyViewHolder(itemView);
            return rcv;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            Log.e("Image url",button_list.get(position).get("url"));
            /*Glide.with(GoogleVision.this)
                    .load(button_list.get(position).get("url"))
                    .override(300, 200)
                    .into(holder.siv_image_item);*/

            Picasso.with(GoogleVision.this).load(button_list.get(position).get("url")).fit().centerCrop().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(holder.siv_image_item);
        }

        @Override
        public int getItemCount()
        {
            return button_list.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView siv_image_item;

        public MyViewHolder(View view)
        {
            super(view);
            siv_image_item = (ImageView) view.findViewById(R.id.xiv_image_item);

            view.setClickable(true);

            siv_image_item.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.e("ItemPositoipn","--"+getLayoutPosition());
                    Toast.makeText(GoogleVision.this, "button_list:"+getLayoutPosition(), Toast.LENGTH_SHORT).show();
                    /*Intent webservice=new Intent(GoogleVision.this,GoogleVision.class);
                    startActivity(webservice);*/
                }
            });

        }

    }



    public class ListAdapter extends RecyclerView.Adapter<MyViewHolder1>
    {
        private ArrayList<HashMap<String,String>> button_list = new ArrayList<>();

        public ListAdapter(ArrayList<HashMap<String,String>> button_list) {
            this.button_list=button_list;
        }

        @Override
        public MyViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_links, parent, false);
            MyViewHolder1 rcv = new MyViewHolder1(itemView);
            return rcv;
        }

        @Override
        public void onBindViewHolder(MyViewHolder1 holder, final int position)
        {
            Log.e("Image url",button_list.get(position).get("url"));

            SpannableString content = new SpannableString(button_list.get(position).get("url"));
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            holder.siv_image_item.setText(content);

            holder.siv_image_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent open_link=new Intent(Intent.ACTION_VIEW, Uri.parse(button_list.get(position).get("url")));
                    startActivity(open_link);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return button_list.size();
        }
    }

    public class MyViewHolder1 extends RecyclerView.ViewHolder
    {
        public TextView siv_image_item;

        public MyViewHolder1(View view)
        {
            super(view);
            siv_image_item = (TextView) view.findViewById(R.id.xtv_pages);

            view.setClickable(true);

            siv_image_item.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.e("ItemPositoipn","--"+getLayoutPosition());
                    Toast.makeText(GoogleVision.this, "button_list:"+getLayoutPosition(), Toast.LENGTH_SHORT).show();
                    /*Intent webservice=new Intent(GoogleVision.this,GoogleVision.class);
                    startActivity(webservice);*/
                }
            });

        }

    }


    public class ListAdapterEntities extends RecyclerView.Adapter<MyViewHolder2>
    {
        private ArrayList<HashMap<String,String>> button_list = new ArrayList<>();

        public ListAdapterEntities(ArrayList<HashMap<String,String>> button_list) {
            this.button_list=button_list;
        }

        @Override
        public MyViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.web_entities, parent, false);
            MyViewHolder2 rcv = new MyViewHolder2(itemView);
            return rcv;
        }

        @Override
        public void onBindViewHolder(MyViewHolder2 holder, int position)
        {
           holder.stv_score.setText("Score: "+button_list.get(position).get("score"));
           holder.stv_desc.setText(button_list.get(position).get("description"));
        }

        @Override
        public int getItemCount()
        {
            return button_list.size();
        }
    }

    public class MyViewHolder2 extends RecyclerView.ViewHolder
    {
        public TextView stv_score,stv_desc;

        public MyViewHolder2(View view)
        {
            super(view);
            stv_score = (TextView) view.findViewById(R.id.xtv_score);
            stv_desc = (TextView) view.findViewById(R.id.xtv_description);

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.google_vision, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // action with ID action_refresh was selected
            case R.id.action_camera:
                // match_images,match_pages,partial_images,web_entities;
                if(match_images.size()!=0)
                {
                    match_images.clear();
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    stv_visuallySimilarImages.setVisibility(View.VISIBLE);
                    srecycler_match_images.setVisibility(View.VISIBLE);
                }
                if(match_pages.size()!=0)
                {
                    match_pages.clear();
                    listAdapter.notifyDataSetChanged();
                }
                else
                {
                    stv_pagesWithMatchingImages.setVisibility(View.VISIBLE);
                    srecycler_pagesWithMatchingImages.setVisibility(View.VISIBLE);
                }
                if(partial_images.size()!=0)
                {
                    partial_images.clear();
                    adapter1.notifyDataSetChanged();
                }
                else
                {
                    stv_partialMatchingImages.setVisibility(View.VISIBLE);
                    srecycler_partialMatchingImages.setVisibility(View.VISIBLE);
                }
                if(web_entities.size()!=0)
                {
                    web_entities.clear();
                    listEntities.notifyDataSetChanged();
                }
                else
                {
                    stv_webEntities.setVisibility(View.VISIBLE);
                    srecycler_webEntities.setVisibility(View.VISIBLE);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleVision.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
                break;
             case R.id.action_face:
                 Intent in=new Intent(GoogleVision.this,FaceDetection.class);
                 startActivity(in);
                 break;
            default:
                break;
        }

        return true;
    }
}
