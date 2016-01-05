package by.khrapovitsky.notesclient.service;

import android.app.IntentService;
import android.content.Intent;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import by.khrapovitsky.notesclient.Constants;
import by.khrapovitsky.notesclient.model.Archive;

public class ListArchivesService extends IntentService {

    public static final String RESPONSE = "myResponse";
    public static final String RESPONSE_STATUS = "myResponseMessage";

    public ListArchivesService() {
        super(ListArchivesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String status = "0";
        try {
            URL listPath = new URL(Constants.URL_LIST);
            Map<String, Archive> archiveHashMap = new HashMap<>();
            Map<String, Archive> output = new HashMap<>();
            archiveHashMap = getListArchives(listPath);
            if (archiveHashMap != null) {
                Iterator iterator = archiveHashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    File tmpFile = new File(Constants.PATH_DIRECTORY, entry.getKey().toString());
                    if (!tmpFile.exists()) {
                        try {
                            File tmpDownloadedFile = downloadArchiveFromServer(entry.getKey().toString());
                            Archive tmpArchive;
                            if(tmpDownloadedFile!=null){
                                tmpArchive = unzip(downloadArchiveFromServer(entry.getKey().toString()),Constants.PATH_DIRECTORY);
                                if(tmpArchive!=null){
                                    output.put(entry.getKey().toString(), tmpArchive);
                                }else{
                                    status = "-1";
                                }
                            }else{
                                status = "-1";
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    iterator.remove();
                }
                if(!output.isEmpty() && status.equals("-1")){
                    status = "2";
                }else{
                    if(!output.isEmpty())
                        status = "1";
                }
            }else{
                status = "-1";
            }
            if(status.equals("1") || status.equals("2")){
                Intent resultBroadCastIntent = new Intent();
                resultBroadCastIntent.setAction(Constants.PROCESS_RESPONSE);
                resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                resultBroadCastIntent.putExtra(RESPONSE, (Serializable) output);
                resultBroadCastIntent.putExtra(RESPONSE_STATUS, status);
                sendBroadcast(resultBroadCastIntent);
            }else{
                Intent resultBroadCastIntent = new Intent();
                resultBroadCastIntent.setAction(Constants.PROCESS_RESPONSE);
                resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                resultBroadCastIntent.putExtra(RESPONSE_STATUS, status);
                sendBroadcast(resultBroadCastIntent);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Archive> getListArchives(URL listPath) {
        Map<String, Archive> archiveHashMap = new HashMap<>();
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) listPath.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                archiveHashMap = parseResult(response);
            }else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return archiveHashMap;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        if (inputStream != null) {
            inputStream.close();
        }
        return result;
    }

    private Map<String, Archive> parseResult(String result) throws JSONException {
        Map<String, Archive> archiveHashMap = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                archiveHashMap.put(explrObject.getString("name"), null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return archiveHashMap;
    }

    private Archive unzip(File zipFile, String location) throws IOException {
        Archive archive = new Archive();
        if(zipFile!=null){
            try {
                File f = new File(location);
                if (!f.isDirectory()) {
                    f.mkdirs();
                }
                ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
                try {
                    ZipEntry ze = null;
                    while ((ze = zin.getNextEntry()) != null) {
                        String path = location + ze.getName();
                        if (ze.isDirectory()) {
                            File unzipFile = new File(path);
                            if (!unzipFile.isDirectory()) {
                                unzipFile.mkdirs();
                            }
                        } else {
                            FileOutputStream fout = new FileOutputStream(path, false);
                            try {
                                for (int c = zin.read(); c != -1; c = zin.read()) {
                                    fout.write(c);
                                }
                                zin.closeEntry();
                            } finally {
                                fout.close();
                            }
                        }
                        File file = new File(location + File.separator + ze.getName());
                        if (file.exists()) {
                            switch (getMimeType(file.getAbsolutePath())) {
                                case "text/plain":
                                    archive.setTxtPath(file.getAbsolutePath());
                                    break;
                                case "image/jpeg":
                                    archive.setImgPath(file.getAbsolutePath());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } finally {
                    zin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return archive;
        }else{
            return null;
        }
    }

    private File downloadArchiveFromServer(String name) throws IOException {
        File directory = new File(Constants.PATH_DIRECTORY);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        HttpURLConnection connection = null;
        File output = null;
        if (!directory.exists()) {
            directory.mkdir();
        }
        output = new File(Constants.PATH_DIRECTORY, name);
        if (output.exists()) {
            return output;
        } else {
            try {
                URL url = new URL(Constants.URL_DOWNLOAD + name);
                connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() != 200)
                    return null;
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(output);
                byte buffer[] = new byte[1024];
                int byteCount;
                while ((byteCount = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteCount);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return output;
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
