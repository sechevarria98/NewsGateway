package com.example.gateway;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryFragment extends Fragment {

    public StoryFragment() {
    }

    static StoryFragment newInstance(Source source, int index, int max)
    {
        StoryFragment f = new StoryFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("STORY", source);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL", max);
        f.setArguments(bdl);
        return f;
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment_layout = inflater.inflate(R.layout.fragment_stories, container, false);

        Bundle args = getArguments();
        if(args != null) {
            final Source currentSource = (Source) args.getSerializable("STORY");
            if(currentSource == null)
                return null;

            final int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL");

            TextView title = fragment_layout.findViewById(R.id.TitleFragment);
            TextView published = fragment_layout.findViewById(R.id.PublishedAtFragment);
            TextView author = fragment_layout.findViewById(R.id.AuthorFragment);
            TextView description = fragment_layout.findViewById(R.id.DescriptionFragment);
            TextView page = fragment_layout.findViewById(R.id.PageFragment);
            final ImageView imageView = fragment_layout.findViewById(R.id.imageView);

            if (!(currentSource.getTitle().equals("") || currentSource.getTitle().equals("null"))) {
                title.setText(currentSource.getTitle());
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(currentSource.getUrl()));
                        startActivity(intent);
                    }
                });
            } else {
                title.setText(getString(R.string.not_prov));
            }

            if (!(currentSource.getAuthor().equals("") || currentSource.getAuthor().equals("null"))) {
                author.setText(currentSource.getAuthor());
            } else {
                author.setText(getString(R.string.not_prov));
            }

            if (!(currentSource.getDescription().equals("") || currentSource.getDescription().equals("null"))) {
                description.setText(currentSource.getDescription());
                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(currentSource.getUrl()));
                        startActivity(intent);
                    }
                });
            } else {
                description.setText(getString(R.string.not_prov));
            }

            if(currentSource.getUrlToImage().equals(""))
                imageView.setVisibility(View.INVISIBLE);
            else {
                Picasso.get().load(currentSource.getUrlToImage())
                        .error(R.drawable.noimagelink)
                        .placeholder(R.drawable.placeholder)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(currentSource.getUrl()));
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                            }
                        });
            }

            String formattedDate = "";
            //These formats should cover most. There may be a few exceptions
            //Couldn't Test Extensively because of limit on how many calls.
            String isoFormatOne = "yyyy-MM-dd'T'HH:mm:ssXXX";
            String isoFormatTwo = "yyyy-MM-dd'T'HH:mm:ss+hh:mm";
            String isoFormatThree = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(isoFormatOne);
                formattedDate = new SimpleDateFormat("MMM d, YYYY HH:mm")
                        .format(Objects.requireNonNull(simpleDateFormat.parse(currentSource.getPublishedAt())));
            } catch (ParseException e) {
                try {
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(isoFormatTwo);
                    formattedDate = new SimpleDateFormat("MMM d, YYYY HH:mm")
                            .format(Objects.requireNonNull(simpleDateFormat1.parse(currentSource.getPublishedAt())));
                } catch (Exception err) {
                    try {
                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(isoFormatThree);
                        formattedDate = new SimpleDateFormat("MMM d, YYYY HH:mm")
                                .format(Objects.requireNonNull(simpleDateFormat2.parse(currentSource.getPublishedAt())));
                    } catch (Exception er) {
                        // For those dates that are a different format that I haven't seen from testing
                        published.setText(currentSource.getPublishedAt());
                    }
                }
            }
            published.setText(formattedDate);

            page.setText(String.format(Locale.US,"%d of %d", index, total));

            return fragment_layout;
        } else {
            return null;
        }
    }
}
