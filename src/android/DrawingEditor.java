package com.yeayyy.handrawing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by asanrivas on 19/06/2015.
 */

public class DrawingEditor extends CordovaPlugin {
    static final int PICK_IMAGE_REQUEST = 1;
    CallbackContext callbackContext;
    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.v("DrawingActivity", "entered my domain");
        if (action.compareTo("openDraw") == 0)
        {
            String message = args.getString(0);

            this.callbackContext = callbackContext;

            Intent i = new Intent();
            Context context=this.cordova.getActivity().getApplicationContext();
            //or Context context=cordova.getActivity().getApplicationContext();
            Intent intent=new Intent(context,MainActivity.class);
            try {
                intent.putExtra("URL", getFileUriAndSetType(message).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //context.startActivity(intent);
            this.cordova.startActivityForResult(this, intent, PICK_IMAGE_REQUEST);
            return true;
        }

        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case PICK_IMAGE_REQUEST: //integer matching the integer suplied when starting the activity
                if(resultCode == android.app.Activity.RESULT_OK){
                    //in case of success return the string to javascript

                    String result=intent.getStringExtra("result");
                    Log.v("pick image", result);
                    this.callbackContext.success(result);
                }
                break;
            default:
                break;
        }
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    private void saveFile(byte[] bytes, String dirName, String fileName) throws IOException {
        final File dir = new File(dirName);
        final FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
    private Uri getFileUriAndSetType(String image) throws IOException {
        // we're assuming an image, but this can be any filetype you like

        String dir = getDownloadDir();
        String localImage = image;
        if (image.startsWith("http") || image.startsWith("www/")) {
            String filename = getFileName(image);
            localImage = "file://" + dir + "/" + filename;
            if (image.startsWith("http")) {
                // filename optimisation taken from https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin/pull/56
                URLConnection connection = new URL(image).openConnection();
                String disposition = connection.getHeaderField("Content-Disposition");
                if (disposition != null) {
                    final Pattern dispositionPattern = Pattern.compile("filename=([^;]+)");
                    Matcher matcher = dispositionPattern.matcher(disposition);
                    if (matcher.find()) {
                        filename = matcher.group(1).replaceAll("[^a-zA-Z0-9._-]", "");
                        localImage = "file://" + dir + "/" + filename;
                    }
                }
                saveFile(getBytes(connection.getInputStream()), dir, filename);
            } else {
                saveFile(getBytes(webView.getContext().getAssets().open(image)), dir, filename);
            }
        } else if (image.startsWith("data:")) {
            // safeguard for https://code.google.com/p/android/issues/detail?id=7901#c43
            if (!image.contains(";base64,")) {
                return null;
            }
            // image looks like this: data:image/png;base64,R0lGODlhDAA...
            final String encodedImg = image.substring(image.indexOf(";base64,") + 8);
            // correct the intent type if anything else was passed, like a pdf: data:application/pdf;base64,..

            // the filename needs a valid extension, so it renders correctly in target apps
            final String imgExtension = image.substring(image.indexOf("/") + 1, image.indexOf(";base64"));
            String fileName;
            fileName = "attach01";
            saveFile(Base64.decode(encodedImg, Base64.DEFAULT), dir, fileName);
            localImage = "file://" + dir + "/" + fileName;
        } else if (!image.startsWith("file://")) {
            throw new IllegalArgumentException("URL_NOT_SUPPORTED");
        }
        return Uri.parse(localImage);
    }

    private byte[] getBytes(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
        int current;
        while ((current = bis.read()) != -1) {
            baf.append((byte) current);
        }
        return baf.toByteArray();
    }
    private String getFileName(String url) {
        final int lastIndexOfSlash = url.lastIndexOf('/');
        if (lastIndexOfSlash == -1) {
            return url;
        } else {
            return url.substring(lastIndexOfSlash + 1);
        }
    }

    private void createOrCleanDir(final String downloadDir) throws IOException {
        final File dir = new File(downloadDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("CREATE_DIRS_FAILED");
            }
        } else {
            cleanupOldFiles(dir);
        }
    }

    private String getDownloadDir() throws IOException {
        final String dir = webView.getContext().getExternalFilesDir(null) + "/handdrawing-downloads"; // external

//    final String dir = webView.getContext().getCacheDir() + "/socialsharing-downloads"; // internal (no external permission needed)
        createOrCleanDir(dir);
        return dir;
    }

    /**
     * As file.deleteOnExit does not work on Android, we need to delete files manually.
     * Deleting them in onActivityResult is not a good idea, because for example a base64 encoded file
     * will not be available for upload to Facebook (it's deleted before it's uploaded).
     * So the best approach is deleting old files when saving (sharing) a new one.
     */
    private void cleanupOldFiles(File dir) {
        for (File f : dir.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }
}