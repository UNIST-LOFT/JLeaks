protected void onCreate(@Nullable Bundle savedInstanceState) 
{
    super.onCreate(savedInstanceState);
    String theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "");
    Logger.dev("AboutActivity: Theme is " + theme);
    if (Utils.isDarkTheme) {
        setTheme(R.style.AppTheme_dh);
    }
    setContentView(R.layout.activity_about);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(view -> finish());
    ActionBar ab = getSupportActionBar();
    if (ab != null) {
        ab.setTitle(R.string.about);
        ab.setDisplayHomeAsUpEnabled(true);
    }
    appVersionInfo.setSummary(BuildConfig.VERSION_NAME);
    String changes = null;
    try (InputStream is = getAssets().open("changelog.html")) {
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        changes = new String(buffer);
    } catch (IOException ignored) {
    }
    appChangelog.removeSummary();
    if (changes == null) {
        appChangelog.setVisibility(View.GONE);
    } else {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(changes, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            result = Html.fromHtml(changes);
        }
        appChangelog.setOnClickListener(v -> {
            AlertDialog d = Utils.getAlertDialogBuilder(this).setTitle(R.string.app_changelog).setMessage(result).setPositiveButton(android.R.string.ok, null).show();
            // noinspection ConstantConditions
            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        });
    }
    appDevelopers.removeSummary();
    appDevelopers.setOnClickListener(view -> {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(getString(R.string.app_developers_), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            result = Html.fromHtml(getString(R.string.app_developers_));
        }
        AlertDialog d = Utils.getAlertDialogBuilder(this).setTitle(R.string.app_developers).setMessage(result).setPositiveButton(android.R.string.ok, null).create();
        d.show();
        // noinspection ConstantConditions
        ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    });
    String translators = getString(R.string.translators);
    if (TextUtils.isEmpty(translators)) {
        appTranslators.setVisibility(View.GONE);
    } else {
        appTranslators.setSummary(translators);
    }
    appSourceCode.removeSummary();
    appSourceCode.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL))));
    supportThread.removeSummary();
    supportThread.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(XDA_THREAD))));
    donation.removeSummary();
    donation.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL))));
    setFloating();
}