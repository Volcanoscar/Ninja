package io.github.mthli.Ninja.Activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.*;
import io.github.mthli.Ninja.Browser.AlbumController;
import io.github.mthli.Ninja.Browser.BrowserContainer;
import io.github.mthli.Ninja.Browser.BrowserController;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Unit.BrowserUnit;
import io.github.mthli.Ninja.Unit.ViewUnit;
import io.github.mthli.Ninja.View.NinjaRelativeLayout;
import io.github.mthli.Ninja.View.NinjaWebView;
import io.github.mthli.Ninja.View.SwipeToDismissListener;
import io.github.mthli.Ninja.View.SwitcherPanel;

public class BrowserActivity extends Activity implements BrowserController {
    private SwitcherPanel switcherPanel;

    private FrameLayout switcherHeader;
    private HorizontalScrollView swictherScroller;
    private LinearLayout switcherContainer;
    private ImageButton addButton;
    private float dimen72dp;
    private float dimen54dp;
    private float dimen48dp;

    private RelativeLayout ominibox;
    private AutoCompleteTextView inputBox;
    private ImageButton bookmarkButton;
    private ImageButton refreshButton;
    private ImageButton overflowButton;
    private LinearLayout progressWrapper;
    private ProgressBar progressBar;
    private FrameLayout contentFrame;

    private RelativeLayout searchPanel;
    private EditText searchBox;
    private ImageButton searchUp;
    private ImageButton searchDown;
    private ImageButton searchCancel;

    private AlbumController currentAlbumController = null;

    private boolean create = true;
    private int animTime = 0;

    @Override
    public void updateBookmarks() {}

    @Override
    public void updateInputBox(String query) {}

    @Override
    public void updateProgress(int progress) {}

    @Override
    public void onCreateView(WebView view, Message resultMsg) {}

    @Override
    public void onLongPress(String url) {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        create = true;
        animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        switcherPanel = (SwitcherPanel) findViewById(R.id.switcher_panel);

        initSwitcherView();
        initMainView();
        initSearchPanel();
        addAlbum(); // TODO
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (switcherPanel.getStatus() != SwitcherPanel.Status.EXPANDED) {
            switcherPanel.expanded();
        }
        int windowHeight = ViewUnit.getWindowHeight(this);
        int statusBarHeight = ViewUnit.getStatusBarHeight(this);
        int navigationBarHeight = ViewUnit.getNavigationBarHeight(this);
        float coverHeight = windowHeight + navigationBarHeight - statusBarHeight - dimen54dp - dimen48dp;
        switcherPanel.setCoverHeight(coverHeight);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (switcherPanel.getStatus() != SwitcherPanel.Status.EXPANDED) {
                switcherPanel.expanded();
            } else {
                finish();
            }
        }
        return true;
    }

    private void initSwitcherView() {
        switcherHeader = (FrameLayout) findViewById(R.id.switcher_header);
        swictherScroller = (HorizontalScrollView) findViewById(R.id.switcher_scoller);
        switcherContainer = (LinearLayout) findViewById(R.id.switcher_container);
        addButton = (ImageButton) findViewById(R.id.switcher_add);

        dimen72dp = ViewUnit.dp2px(this, getResources().getDimension(R.dimen.layout_width_72dp));
        dimen54dp = ViewUnit.dp2px(this, getResources().getDimension(R.dimen.layout_height_54dp));
        dimen48dp = ViewUnit.dp2px(this, getResources().getDimension(R.dimen.layout_height_48dp));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlbum();
            }
        });
    }

    private void initMainView() {
        ominibox = (RelativeLayout) findViewById(R.id.main_omnibox);
        inputBox = (AutoCompleteTextView) findViewById(R.id.main_omnibox_input);
        bookmarkButton = (ImageButton) findViewById(R.id.main_omnibox_bookmark);
        refreshButton = (ImageButton) findViewById(R.id.main_omnibox_refresh);
        overflowButton = (ImageButton) findViewById(R.id.main_omnibox_overflow);
        progressWrapper = (LinearLayout) findViewById(R.id.main_progress_wrapper);
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        contentFrame = (FrameLayout) findViewById(R.id.main_content);
    }

    private void initSearchPanel() {
        searchPanel = (RelativeLayout) getLayoutInflater().inflate(R.layout.search, null, false);
        searchBox = (EditText) searchPanel.findViewById(R.id.search_box);
        searchUp = (ImageButton) searchPanel.findViewById(R.id.search_up);
        searchDown = (ImageButton) searchPanel.findViewById(R.id.search_down);
        searchCancel = (ImageButton) searchPanel.findViewById(R.id.search_cancel);
    }

    private synchronized void addAlbum() {
        final NinjaRelativeLayout homeLayout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.home, null, false);
        homeLayout.setBrowserController(this);
        homeLayout.setFlag(BrowserUnit.FLAG_HOME);
        homeLayout.setAlbumCover(ViewUnit.capture(homeLayout, dimen72dp, dimen54dp));
        homeLayout.setAlbumTitle(getString(R.string.album_title_home));

        final View albumView = homeLayout.getAlbumView();
        albumView.setVisibility(View.INVISIBLE);
        albumView.setOnTouchListener(new SwipeToDismissListener(
                albumView,
                null,
                new SwipeToDismissListener.DismissCallback() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        // TODO: removeAlbum()
                    }
                }
        ));

        BrowserContainer.add(homeLayout);
        switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT); // TODO

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showAlbum(homeLayout, true);
            }
        });
        albumView.startAnimation(animation);
    }

    private synchronized void addAlbum(String title, final String url, final boolean foreground, final Message resultMsg) {
        final NinjaWebView ninjaWebView = new NinjaWebView(this);
        ninjaWebView.setBrowserController(this);
        ninjaWebView.setFlag(BrowserUnit.FLAG_NINJA);
        ninjaWebView.setAlbumCover(null);
        ninjaWebView.setAlbumTitle(title);

        final View albumView = ninjaWebView.getAlbumView();
        albumView.setVisibility(View.INVISIBLE);

        BrowserContainer.add(ninjaWebView);
        switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!foreground) {
                    ninjaWebView.loadUrl(url);
                    ninjaWebView.deactivate();
                    if (currentAlbumController != null) {
                        swictherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
                    }
                    return;
                }

                showAlbum(ninjaWebView, true);

                if (url != null && !url.isEmpty()) {
                    ninjaWebView.loadUrl(url);
                } else if (resultMsg != null) {
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(ninjaWebView);
                    resultMsg.sendToTarget();
                }
            }
        });
        albumView.startAnimation(animation);
    }

    @Override
    public synchronized void showAlbum(AlbumController albumController, boolean scroll) {
        if (albumController == null || !(albumController instanceof View) || albumController.equals(currentAlbumController)) {
            return;
        }

        if (currentAlbumController != null) {
            currentAlbumController.deactivate();
        }
        contentFrame.removeAllViews();
        contentFrame.addView((View) albumController);
        currentAlbumController = albumController;
        currentAlbumController.activate();
        if (scroll) {
            swictherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0); // TODO
        }
        switcherPanel.expanded();
    }
}
