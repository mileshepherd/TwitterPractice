package com.example.angus.tweet;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText m_search_key;
    private ImageButton m_btn_ok;
    private TextView m_header;
    private ListView m_tweet_list;
    private InputMethodManager im_ctrl;
    private JSONArray jresult;
    private LayoutInflater m_inflater;
    private ImageWorker iw;

    private Pattern fromUserPat = Pattern.compile("^[A-Za-z0-9_]+");
    private Pattern userPat = Pattern.compile("^[A-Za-z0-9_]+");
    private Pattern hashPat = Pattern.compile("#[A-Za-z0-9_]+");
    private Pattern urlPat = Pattern.compile("http://[^ ]+");

    private String tweetUserURL = "https://twitter.com/";
    private String tweetSearchURL = "https://twitter.com/search?q=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_search_key = (EditText)findViewById(R.id.search_key);
        m_btn_ok =(ImageButton)findViewById(R.id.btn_ok);
        m_tweet_list = (ListView)findViewById(R.id.tweet_list);
        m_header = (TextView)findViewById(R.id.tweet_header);
        m_btn_ok.setOnClickListener(ok_handler);
        im_ctrl = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        m_inflater = LayoutInflater.from(this);
        iw = new ImageWorker(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void alert(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    View.OnClickListener ok_handler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //im_ctrl.hideSoftInputFromInputMethod(m_search_key.getWindowToken(),0);
            hideSoftKeyboard(MainActivity.this);
            TwitterTask tweet_task = new TwitterTask();
            try{
                tweet_task.execute(m_search_key.getText().toString());
                //tweet_task.get(20000, TimeUnit.MILLISECONDS);
            }catch(Exception e){
                tweet_task.cancel(true);
                alert("Cannot retrieve tweets right now please try again later");
            }

            Toast.makeText(getApplicationContext(),getString(R.string.hello_world)+" "
                    +m_search_key.getText(),Toast.LENGTH_LONG).show();
        }
    };

    private class TwitterTask extends AsyncTask<String,Integer,String>{
        @Override
        protected void onPreExecute(){
            //// TODO: 16/1/28 AUTO-GENERATED METHOD STUB
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params){
            String query = params[0];
            if(query==null)return"";
            try{
                System.setProperty("jsse.enableSNIExtension", "false");
                String result = TwitterHelper.downloadFromServer(query);
                Log.e("response body",result);
                return result;
            }catch(TwitterHelper.ApiException e){
                e.printStackTrace();
                Log.e("HSD","Problem making twitter search request");
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result){
            try{
                JSONObject object = new JSONObject(result);
                jresult = object.getJSONArray("statuses");
                //String info = object.getString("search_metadata");
                //m_header.setText(info);
                if(jresult==null||jresult.length()==0) {
                    Toast.makeText(getApplicationContext(), "No tweets on " +
                            m_search_key.getText(), Toast.LENGTH_LONG).show();
                }else{
                    m_tweet_list.setAdapter(new JSONAdapter(getApplication()));
                }
            }catch(JSONException e){
                alert("onPostExecute():" + e.getMessage());
            }
        }
    }

    private Linkify.TransformFilter noAtSign = new Linkify.TransformFilter(){
        @Override
        public String transformUrl(Matcher match,String user){
            return user.substring(1);
        }
    };

    private Linkify.TransformFilter hashEncoder = new Linkify.TransformFilter(){
        @Override
        public String transformUrl(Matcher match,String hash){
            return Uri.encode(hash);
        }
    };

    static class ViewHolder{
        TextView text, time;
        ImageView icon;
    }



    private class JSONAdapter extends BaseAdapter{
        private Context mCtx;
        public JSONAdapter(Context c){
            mCtx = c;
        }

        @Override
        public  int getCount(){
            return jresult == null? 0:jresult.length();
        }

        @Override
        public Object getItem(int arg0){return null;}

        @Override
        public long getItemId(int pos){return pos;}

        /*
        @Override
        public View getView(int pos,View convertView,ViewGroup parent){
            TextView tv;
            if(convertView == null)
                tv = new TextView(mCtx);
            else
                tv = (TextView)convertView;
                try{
                    JSONObject object = jresult.getJSONObject(pos);
                    JSONObject user = object.getJSONObject("user");
                    String name = user.getString("name");
                    tv.setText(object.getString("created_at") + " #" + name + "\n"+ object.getString("text"));
                }catch(JSONException e){
                    tv.setText(e.getMessage());
                }
            return tv;
        }*/

        @Override
        public View getView(int pos,View convertView, ViewGroup parent){
            ImageView photo;
            ViewHolder holder;
            if(convertView == null){
                convertView = m_inflater.inflate(R.layout.item,parent,false);
                holder = new ViewHolder();
                holder.text = (TextView)convertView.findViewById(R.id.text);
                holder.time = (TextView)convertView.findViewById(R.id.created_at);
                holder.icon = (ImageView)convertView.findViewById(R.id.user_photo);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            try{
                JSONObject obj = jresult.getJSONObject(pos);
                JSONObject user = obj.getJSONObject("user");
                String username = user.getString("name");
                TextView t;
                t = holder.text;
                t.setText(username + ":" + Html.fromHtml(obj.getString("text")));
                Linkify.addLinks(t, fromUserPat, tweetUserURL);
                Linkify.addLinks(t, userPat, tweetUserURL, null, noAtSign);
                Linkify.addLinks(t,hashPat,tweetSearchURL,null,hashEncoder);
                Linkify.addLinks(t,urlPat,"");
                photo = holder.icon;

                photo.setTag(user.getString("profile_image_url_https"));
                Drawable dr = iw.loadImage(this,photo);
                photo.setImageDrawable(dr);
                holder.time.setText(obj.getString("created_at"));
            }catch (JSONException e){
                Log.e("HSD","Error in getView():" + e.getMessage());
            }
            return convertView;
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
