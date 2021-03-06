package com.PopCorp.Purchases.data.model.skidkaonline;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Shop implements Parcelable {

    @SerializedName("name")
    private String name;
    @SerializedName("url")
    private String url;
    @SerializedName("image")
    private String image;
    @SerializedName("category")
    private Category category;
    @SerializedName("cityUrl")
    private String cityUrl;
    @SerializedName("cityId")
    private int cityId;

    private boolean favorite = false;

    public Shop(String name, String url, String image, Category category, String cityUrl, int cityId, boolean favorite) {
        this.name = name;
        this.url = url;
        this.image = image;
        this.category = category;
        this.cityUrl = cityUrl;
        this.cityId = cityId;
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Shop)) return false;
        Shop shop = (Shop) object;
        return url.equals(shop.getUrl()) && cityId == shop.getCityId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(image);
        dest.writeParcelable(category, flags);
        dest.writeString(cityUrl);
        dest.writeInt(cityId);
        dest.writeString(String.valueOf(favorite));
    }

    public static final Creator<Shop> CREATOR = new Creator<Shop>() {
        public Shop createFromParcel(Parcel in) {
            return new Shop(in);
        }

        public Shop[] newArray(int size) {
            return new Shop[size];
        }
    };

    private Shop(Parcel parcel) {
        this.name = parcel.readString();
        this.url = parcel.readString();
        this.image = parcel.readString();
        this.category = parcel.readParcelable(Category.class.getClassLoader());
        this.cityUrl = parcel.readString();
        this.cityId = parcel.readInt();
        this.favorite = Boolean.parseBoolean(parcel.readString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCityUrl() {
        return cityUrl;
    }

    public void setCityUrl(String cityUrl) {
        this.cityUrl = cityUrl;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
