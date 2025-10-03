package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.core.model.ModelIdentifier;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the UserPhoto type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "UserPhotos", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
}, hasLazySupport = true)
public final class UserPhoto implements Model {
  public static final UserPhotoPath rootPath = new UserPhotoPath("root", false, null);
  public static final QueryField ID = field("UserPhoto", "id");
  public static final QueryField USER_ID = field("UserPhoto", "userId");
  public static final QueryField PHOTO_URL = field("UserPhoto", "photoUrl");
  public static final QueryField IS_DEFAULT = field("UserPhoto", "isDefault");
  public static final QueryField UPLOADED_AT = field("UserPhoto", "uploadedAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String userId;
  private final @ModelField(targetType="String", isRequired = true) String photoUrl;
  private final @ModelField(targetType="Boolean") Boolean isDefault;
  private final @ModelField(targetType="AWSDateTime", isRequired = true) Temporal.DateTime uploadedAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  /** @deprecated This API is internal to Amplify and should not be used. */
  @Deprecated
   public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public String getUserId() {
      return userId;
  }
  
  public String getPhotoUrl() {
      return photoUrl;
  }
  
  public Boolean getIsDefault() {
      return isDefault;
  }
  
  public Temporal.DateTime getUploadedAt() {
      return uploadedAt;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private UserPhoto(String id, String userId, String photoUrl, Boolean isDefault, Temporal.DateTime uploadedAt) {
    this.id = id;
    this.userId = userId;
    this.photoUrl = photoUrl;
    this.isDefault = isDefault;
    this.uploadedAt = uploadedAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      UserPhoto userPhoto = (UserPhoto) obj;
      return ObjectsCompat.equals(getId(), userPhoto.getId()) &&
              ObjectsCompat.equals(getUserId(), userPhoto.getUserId()) &&
              ObjectsCompat.equals(getPhotoUrl(), userPhoto.getPhotoUrl()) &&
              ObjectsCompat.equals(getIsDefault(), userPhoto.getIsDefault()) &&
              ObjectsCompat.equals(getUploadedAt(), userPhoto.getUploadedAt()) &&
              ObjectsCompat.equals(getCreatedAt(), userPhoto.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), userPhoto.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserId())
      .append(getPhotoUrl())
      .append(getIsDefault())
      .append(getUploadedAt())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("UserPhoto {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userId=" + String.valueOf(getUserId()) + ", ")
      .append("photoUrl=" + String.valueOf(getPhotoUrl()) + ", ")
      .append("isDefault=" + String.valueOf(getIsDefault()) + ", ")
      .append("uploadedAt=" + String.valueOf(getUploadedAt()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static UserIdStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static UserPhoto justId(String id) {
    return new UserPhoto(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userId,
      photoUrl,
      isDefault,
      uploadedAt);
  }
  public interface UserIdStep {
    PhotoUrlStep userId(String userId);
  }
  

  public interface PhotoUrlStep {
    UploadedAtStep photoUrl(String photoUrl);
  }
  

  public interface UploadedAtStep {
    BuildStep uploadedAt(Temporal.DateTime uploadedAt);
  }
  

  public interface BuildStep {
    UserPhoto build();
    BuildStep id(String id);
    BuildStep isDefault(Boolean isDefault);
  }
  

  public static class Builder implements UserIdStep, PhotoUrlStep, UploadedAtStep, BuildStep {
    private String id;
    private String userId;
    private String photoUrl;
    private Temporal.DateTime uploadedAt;
    private Boolean isDefault;
    public Builder() {
      
    }
    
    private Builder(String id, String userId, String photoUrl, Boolean isDefault, Temporal.DateTime uploadedAt) {
      this.id = id;
      this.userId = userId;
      this.photoUrl = photoUrl;
      this.isDefault = isDefault;
      this.uploadedAt = uploadedAt;
    }
    
    @Override
     public UserPhoto build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new UserPhoto(
          id,
          userId,
          photoUrl,
          isDefault,
          uploadedAt);
    }
    
    @Override
     public PhotoUrlStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userId = userId;
        return this;
    }
    
    @Override
     public UploadedAtStep photoUrl(String photoUrl) {
        Objects.requireNonNull(photoUrl);
        this.photoUrl = photoUrl;
        return this;
    }
    
    @Override
     public BuildStep uploadedAt(Temporal.DateTime uploadedAt) {
        Objects.requireNonNull(uploadedAt);
        this.uploadedAt = uploadedAt;
        return this;
    }
    
    @Override
     public BuildStep isDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String userId, String photoUrl, Boolean isDefault, Temporal.DateTime uploadedAt) {
      super(id, userId, photoUrl, isDefault, uploadedAt);
      Objects.requireNonNull(userId);
      Objects.requireNonNull(photoUrl);
      Objects.requireNonNull(uploadedAt);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder photoUrl(String photoUrl) {
      return (CopyOfBuilder) super.photoUrl(photoUrl);
    }
    
    @Override
     public CopyOfBuilder uploadedAt(Temporal.DateTime uploadedAt) {
      return (CopyOfBuilder) super.uploadedAt(uploadedAt);
    }
    
    @Override
     public CopyOfBuilder isDefault(Boolean isDefault) {
      return (CopyOfBuilder) super.isDefault(isDefault);
    }
  }
  

  public static class UserPhotoIdentifier extends ModelIdentifier<UserPhoto> {
    private static final long serialVersionUID = 1L;
    public UserPhotoIdentifier(String id) {
      super(id);
    }
  }
  
}
