package com.zlm.hp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.zlm.hp.http.api.SubtitleHttpClient;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SubtitleApiTest {

    @Test
    public void searchSubtitle() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SubtitleHttpClient subtitleHttpClient = new SubtitleHttpClient();
        subtitleHttpClient.searchSubtitle(appContext,"big",1,20,true);
    }
}
