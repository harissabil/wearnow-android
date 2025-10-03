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

/** This is an auto generated class representing the TryOnHistory type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "TryOnHistories", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
}, hasLazySupport = true)
public final class TryOnHistory implements Model {
  public static final TryOnHistoryPath rootPath = new TryOnHistoryPath("root", false, null);
  public static final QueryField ID = field("TryOnHistory", "id");
  public static final QueryField USER_ID = field("TryOnHistory", "userId");
  public static final QueryField USER_PHOTO_ID = field("TryOnHistory", "userPhotoId");
  public static final QueryField USER_PHOTO_URL = field("TryOnHistory", "userPhotoUrl");
  public static final QueryField GARMENT_PHOTO_URL = field("TryOnHistory", "garmentPhotoUrl");
  public static final QueryField RESULT_PHOTO_URL = field("TryOnHistory", "resultPhotoUrl");
  public static final QueryField STATUS = field("TryOnHistory", "status");
  public static final QueryField ERROR_MESSAGE = field("TryOnHistory", "errorMessage");
  public static final QueryField METADATA = field("TryOnHistory", "metadata");
  public static final QueryField COMPLETED_AT = field("TryOnHistory", "completedAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String userId;
  private final @ModelField(targetType="String", isRequired = true) String userPhotoId;
  private final @ModelField(targetType="String", isRequired = true) String userPhotoUrl;
  private final @ModelField(targetType="String", isRequired = true) String garmentPhotoUrl;
  private final @ModelField(targetType="String") String resultPhotoUrl;
  private final @ModelField(targetType="TryOnHistoryStatus") TryOnHistoryStatus status;
  private final @ModelField(targetType="String") String errorMessage;
  private final @ModelField(targetType="AWSJSON") String metadata;
  private final @ModelField(targetType="AWSDateTime") Temporal.DateTime completedAt;
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
  
  public String getUserPhotoId() {
      return userPhotoId;
  }
  
  public String getUserPhotoUrl() {
      return userPhotoUrl;
  }
  
  public String getGarmentPhotoUrl() {
      return garmentPhotoUrl;
  }
  
  public String getResultPhotoUrl() {
      return resultPhotoUrl;
  }
  
  public TryOnHistoryStatus getStatus() {
      return status;
  }
  
  public String getErrorMessage() {
      return errorMessage;
  }
  
  public String getMetadata() {
      return metadata;
  }
  
  public Temporal.DateTime getCompletedAt() {
      return completedAt;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private TryOnHistory(String id, String userId, String userPhotoId, String userPhotoUrl, String garmentPhotoUrl, String resultPhotoUrl, TryOnHistoryStatus status, String errorMessage, String metadata, Temporal.DateTime completedAt) {
    this.id = id;
    this.userId = userId;
    this.userPhotoId = userPhotoId;
    this.userPhotoUrl = userPhotoUrl;
    this.garmentPhotoUrl = garmentPhotoUrl;
    this.resultPhotoUrl = resultPhotoUrl;
    this.status = status;
    this.errorMessage = errorMessage;
    this.metadata = metadata;
    this.completedAt = completedAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      TryOnHistory tryOnHistory = (TryOnHistory) obj;
      return ObjectsCompat.equals(getId(), tryOnHistory.getId()) &&
              ObjectsCompat.equals(getUserId(), tryOnHistory.getUserId()) &&
              ObjectsCompat.equals(getUserPhotoId(), tryOnHistory.getUserPhotoId()) &&
              ObjectsCompat.equals(getUserPhotoUrl(), tryOnHistory.getUserPhotoUrl()) &&
              ObjectsCompat.equals(getGarmentPhotoUrl(), tryOnHistory.getGarmentPhotoUrl()) &&
              ObjectsCompat.equals(getResultPhotoUrl(), tryOnHistory.getResultPhotoUrl()) &&
              ObjectsCompat.equals(getStatus(), tryOnHistory.getStatus()) &&
              ObjectsCompat.equals(getErrorMessage(), tryOnHistory.getErrorMessage()) &&
              ObjectsCompat.equals(getMetadata(), tryOnHistory.getMetadata()) &&
              ObjectsCompat.equals(getCompletedAt(), tryOnHistory.getCompletedAt()) &&
              ObjectsCompat.equals(getCreatedAt(), tryOnHistory.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), tryOnHistory.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserId())
      .append(getUserPhotoId())
      .append(getUserPhotoUrl())
      .append(getGarmentPhotoUrl())
      .append(getResultPhotoUrl())
      .append(getStatus())
      .append(getErrorMessage())
      .append(getMetadata())
      .append(getCompletedAt())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("TryOnHistory {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userId=" + String.valueOf(getUserId()) + ", ")
      .append("userPhotoId=" + String.valueOf(getUserPhotoId()) + ", ")
      .append("userPhotoUrl=" + String.valueOf(getUserPhotoUrl()) + ", ")
      .append("garmentPhotoUrl=" + String.valueOf(getGarmentPhotoUrl()) + ", ")
      .append("resultPhotoUrl=" + String.valueOf(getResultPhotoUrl()) + ", ")
      .append("status=" + String.valueOf(getStatus()) + ", ")
      .append("errorMessage=" + String.valueOf(getErrorMessage()) + ", ")
      .append("metadata=" + String.valueOf(getMetadata()) + ", ")
      .append("completedAt=" + String.valueOf(getCompletedAt()) + ", ")
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
  public static TryOnHistory justId(String id) {
    return new TryOnHistory(
      id,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userId,
      userPhotoId,
      userPhotoUrl,
      garmentPhotoUrl,
      resultPhotoUrl,
      status,
      errorMessage,
      metadata,
      completedAt);
  }
  public interface UserIdStep {
    UserPhotoIdStep userId(String userId);
  }
  

  public interface UserPhotoIdStep {
    UserPhotoUrlStep userPhotoId(String userPhotoId);
  }
  

  public interface UserPhotoUrlStep {
    GarmentPhotoUrlStep userPhotoUrl(String userPhotoUrl);
  }
  

  public interface GarmentPhotoUrlStep {
    BuildStep garmentPhotoUrl(String garmentPhotoUrl);
  }
  

  public interface BuildStep {
    TryOnHistory build();
    BuildStep id(String id);
    BuildStep resultPhotoUrl(String resultPhotoUrl);
    BuildStep status(TryOnHistoryStatus status);
    BuildStep errorMessage(String errorMessage);
    BuildStep metadata(String metadata);
    BuildStep completedAt(Temporal.DateTime completedAt);
  }
  

  public static class Builder implements UserIdStep, UserPhotoIdStep, UserPhotoUrlStep, GarmentPhotoUrlStep, BuildStep {
    private String id;
    private String userId;
    private String userPhotoId;
    private String userPhotoUrl;
    private String garmentPhotoUrl;
    private String resultPhotoUrl;
    private TryOnHistoryStatus status;
    private String errorMessage;
    private String metadata;
    private Temporal.DateTime completedAt;
    public Builder() {
      
    }
    
    private Builder(String id, String userId, String userPhotoId, String userPhotoUrl, String garmentPhotoUrl, String resultPhotoUrl, TryOnHistoryStatus status, String errorMessage, String metadata, Temporal.DateTime completedAt) {
      this.id = id;
      this.userId = userId;
      this.userPhotoId = userPhotoId;
      this.userPhotoUrl = userPhotoUrl;
      this.garmentPhotoUrl = garmentPhotoUrl;
      this.resultPhotoUrl = resultPhotoUrl;
      this.status = status;
      this.errorMessage = errorMessage;
      this.metadata = metadata;
      this.completedAt = completedAt;
    }
    
    @Override
     public TryOnHistory build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new TryOnHistory(
          id,
          userId,
          userPhotoId,
          userPhotoUrl,
          garmentPhotoUrl,
          resultPhotoUrl,
          status,
          errorMessage,
          metadata,
          completedAt);
    }
    
    @Override
     public UserPhotoIdStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userId = userId;
        return this;
    }
    
    @Override
     public UserPhotoUrlStep userPhotoId(String userPhotoId) {
        Objects.requireNonNull(userPhotoId);
        this.userPhotoId = userPhotoId;
        return this;
    }
    
    @Override
     public GarmentPhotoUrlStep userPhotoUrl(String userPhotoUrl) {
        Objects.requireNonNull(userPhotoUrl);
        this.userPhotoUrl = userPhotoUrl;
        return this;
    }
    
    @Override
     public BuildStep garmentPhotoUrl(String garmentPhotoUrl) {
        Objects.requireNonNull(garmentPhotoUrl);
        this.garmentPhotoUrl = garmentPhotoUrl;
        return this;
    }
    
    @Override
     public BuildStep resultPhotoUrl(String resultPhotoUrl) {
        this.resultPhotoUrl = resultPhotoUrl;
        return this;
    }
    
    @Override
     public BuildStep status(TryOnHistoryStatus status) {
        this.status = status;
        return this;
    }
    
    @Override
     public BuildStep errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
    
    @Override
     public BuildStep metadata(String metadata) {
        this.metadata = metadata;
        return this;
    }
    
    @Override
     public BuildStep completedAt(Temporal.DateTime completedAt) {
        this.completedAt = completedAt;
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
    private CopyOfBuilder(String id, String userId, String userPhotoId, String userPhotoUrl, String garmentPhotoUrl, String resultPhotoUrl, TryOnHistoryStatus status, String errorMessage, String metadata, Temporal.DateTime completedAt) {
      super(id, userId, userPhotoId, userPhotoUrl, garmentPhotoUrl, resultPhotoUrl, status, errorMessage, metadata, completedAt);
      Objects.requireNonNull(userId);
      Objects.requireNonNull(userPhotoId);
      Objects.requireNonNull(userPhotoUrl);
      Objects.requireNonNull(garmentPhotoUrl);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder userPhotoId(String userPhotoId) {
      return (CopyOfBuilder) super.userPhotoId(userPhotoId);
    }
    
    @Override
     public CopyOfBuilder userPhotoUrl(String userPhotoUrl) {
      return (CopyOfBuilder) super.userPhotoUrl(userPhotoUrl);
    }
    
    @Override
     public CopyOfBuilder garmentPhotoUrl(String garmentPhotoUrl) {
      return (CopyOfBuilder) super.garmentPhotoUrl(garmentPhotoUrl);
    }
    
    @Override
     public CopyOfBuilder resultPhotoUrl(String resultPhotoUrl) {
      return (CopyOfBuilder) super.resultPhotoUrl(resultPhotoUrl);
    }
    
    @Override
     public CopyOfBuilder status(TryOnHistoryStatus status) {
      return (CopyOfBuilder) super.status(status);
    }
    
    @Override
     public CopyOfBuilder errorMessage(String errorMessage) {
      return (CopyOfBuilder) super.errorMessage(errorMessage);
    }
    
    @Override
     public CopyOfBuilder metadata(String metadata) {
      return (CopyOfBuilder) super.metadata(metadata);
    }
    
    @Override
     public CopyOfBuilder completedAt(Temporal.DateTime completedAt) {
      return (CopyOfBuilder) super.completedAt(completedAt);
    }
  }
  

  public static class TryOnHistoryIdentifier extends ModelIdentifier<TryOnHistory> {
    private static final long serialVersionUID = 1L;
    public TryOnHistoryIdentifier(String id) {
      super(id);
    }
  }
  
}
