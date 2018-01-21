package com.ytempest.recycleranalysis.commonRecyclerUse;

/**
 * @author ytempest
 *         Description：
 */

public class ChannelData {
    private int totalUpdates;
    private int name;
    private int intro;
    private boolean isRecommend;
    private int subscribeCount;

    public ChannelData(int totalUpdates, int name, int intro, boolean isRecommend, int subscribeCount) {
        this.totalUpdates = totalUpdates;
        this.name = name;
        this.intro = intro;
        this.isRecommend = isRecommend;
        this.subscribeCount = subscribeCount;
    }

    public int getSubscribeCount() {
        return subscribeCount;
    }

    public void setSubscribeCount(int subscribeCount) {
        this.subscribeCount = subscribeCount;
    }

    public int getTotalUpdates() {
        return totalUpdates;
    }

    public void setTotalUpdates(int totalUpdates) {
        this.totalUpdates = totalUpdates;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getIntro() {
        return intro;
    }

    public void setIntro(int intro) {
        this.intro = intro;
    }

    public boolean isIs_recommend() {
        return isRecommend;
    }

    public void setIs_recommend(boolean is_recommend) {
        isRecommend = is_recommend;
    }
}
