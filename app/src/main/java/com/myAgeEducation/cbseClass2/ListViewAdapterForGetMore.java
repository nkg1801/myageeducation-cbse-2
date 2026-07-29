package com.myAgeEducation.cbseClass2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapterForGetMore extends BaseAdapter
{
    private Activity context;
    private ArrayList<Integer> _appImage;
    private ArrayList<String> _appTitle;
    private ArrayList<String> _appDescription;
    private ArrayList<String> _appLink;
    private Context _context;
    ArrayList<GetMoreApps> _getMoreApps;

    ListViewAdapterForGetMore(Activity context, ArrayList<Integer> appImage, ArrayList<String> appTitle, ArrayList<String> appDescription, ArrayList<String> appLink) {
        super();
        this.context = context;
        this._appImage = appImage;
        this._appTitle = appTitle;
        this._appDescription = appDescription;
        this._appLink = appLink;
        _context = context;
    }

    ListViewAdapterForGetMore(Activity context, ArrayList<GetMoreApps> getMoreApps)// ArrayList<String> appTitle, ArrayList<String> appDescription, ArrayList<String> appLink) {
    {
        super();
        this.context = context;
        _getMoreApps = getMoreApps;
        _context = context;
    }

    public int getCount() {
        return _getMoreApps.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView textViewAppTitle;
        TextView textViewAppDescription;
        ImageView imageViewAppImage;
        Button buttonGetIt;
        Button buttonShare;
    }

    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        final ViewHolder holder;
        LayoutInflater inflater =  context.getLayoutInflater();

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listitem_getmore, null);
            holder = new ViewHolder();
            holder.textViewAppTitle = convertView.findViewById(R.id.textViewAppTitle);
            holder.textViewAppDescription = convertView.findViewById(R.id.textViewAppDescription);
            holder.imageViewAppImage = convertView.findViewById(R.id.imageViewAppImage);
            holder.buttonGetIt = convertView.findViewById(R.id.buttonGetItNow);
            holder.buttonShare = convertView.findViewById(R.id.buttonShare);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textViewAppTitle.setText(_getMoreApps.get(position).getAppName());
        holder.textViewAppDescription.setText(_getMoreApps.get(position).getAppDescription());

        String imageData = _getMoreApps.get(position).getAppImage();
        if(imageData.length() < 100)
        {
            int resourceIdentifier = _context.getResources().getIdentifier(imageData, "drawable", _context.getPackageName());
            if(resourceIdentifier != 0)
            {
                holder.imageViewAppImage.setImageResource(resourceIdentifier);
            }
        }
        else
        {
            holder.imageViewAppImage.setImageBitmap(Util.LoadBitmapFromBase64Encoding(imageData));
        }

        holder.buttonGetIt.setTag(_getMoreApps.get(position).getAppLink());
        String button1Text = _getMoreApps.get(position).getButton1Text();
        if(TextUtils.isEmpty(button1Text))
        {
            holder.buttonShare.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.buttonShare.setVisibility(View.VISIBLE);
        }

        String button2Text = _getMoreApps.get(position).getButton2Text();
        if(TextUtils.isEmpty(button2Text))
        {
            holder.buttonGetIt.setText("Get it now");
        }
        else
        {
            holder.buttonGetIt.setText(button2Text);
        }

        holder.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FirebaseManager.updateGetMoreClicked(_getMoreApps.get(position).getAppName());
                shareAppLink(_getMoreApps.get(position).getShareMessage(), _getMoreApps.get(position).getAppLink());
            }
        });

        holder.buttonGetIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FirebaseManager.updateGetMoreClicked(_getMoreApps.get(position).getAppName());
                openPlayStoreApp(_getMoreApps.get(position).getAppLink());
            }
        });

        return convertView;
    }

    private void shareAppLink(String message, String link)
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = message + System.getProperty("line.separator") + link;
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, Util.ShareLinkTitle);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        _context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        //setShareIntent(sharingIntent);
    }

    private void openPlayStoreApp(String appLink)
    {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(appLink));
            _context.startActivity(intent);
        }
        catch(Exception e)
        {
            displayAlert(e.getMessage(), "Error", _context);
        }
    }

    public void displayAlert(String message, String title, Context context)
    {
        if(!((Activity)context).isFinishing()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setMessage(message);
            alert.setTitle(title);
            alert.setPositiveButton("OK", null);
            alert.setCancelable(true);
            alert.create().show();
        }
    }
}
