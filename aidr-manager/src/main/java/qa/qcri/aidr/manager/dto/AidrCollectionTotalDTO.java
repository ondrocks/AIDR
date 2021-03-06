package qa.qcri.aidr.manager.dto;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import qa.qcri.aidr.manager.persistence.entities.CrisisType;
import qa.qcri.aidr.manager.persistence.entities.UserAccount;
import qa.qcri.aidr.manager.util.CollectionStatus;
import qa.qcri.aidr.manager.util.CollectionType;
import qa.qcri.aidr.manager.util.JsonDateSerializer;

public class AidrCollectionTotalDTO {

    private Long id;

    private String code;

    private String name;

    private String target;

    private UserAccount user;

    private Integer count;

    private CollectionStatus status;

    private String track;

    private String follow;

    private String geo;

    private String langFilters;

    private Date startDate;

    private Date endDate;

    private Date createdDate;

    private String lastDocument;

    private Integer totalCount;

    private Integer taggersCount;

    private List<UserAccount> managers;

    private Integer durationHours;
    
    private Boolean publiclyListed;

    private CrisisType crisisType;

    private String crisisTypeName;

    private boolean hasTaggerOutput;

    private CollectionType collectionType;

    private Integer classifiersNumber;
    
    private String geoR;

    private String purpose;
    
    private int fetchInterval;
    
    private int fetchFrom;
    
    private long totalImageCount;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public CollectionStatus getStatus() {
        return status;
    }

    public void setStatus(CollectionStatus status) {
        this.status = status;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String getLangFilters() {
        return langFilters;
    }

    public void setLangFilters(String langFilters) {
        this.langFilters = langFilters;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastDocument() {
        return lastDocument;
    }

    public void setLastDocument(String lastDocument) {
        this.lastDocument = lastDocument;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTaggersCount() {
        return taggersCount;
    }

    public void setTaggersCount(Integer taggersCount) {
        this.taggersCount = taggersCount;
    }

    public List<UserAccount> getManagers() {
        return managers;
    }

    public void setManagers(List<UserAccount> managers) {
        this.managers = managers;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }
    
    public Boolean getPubliclyListed() {
    	return publiclyListed;
    }
    
    public void setPubliclyListed(Boolean publiclyListed) {
    	this.publiclyListed = publiclyListed;
    }

    public CrisisType getCrisisType() {
        return crisisType;
    }

    public void setCrisisType(CrisisType crisisType) {
        this.crisisType = crisisType;
    }

    public String getCrisisTypeName() {
        return crisisTypeName;
    }

    public void setCrisisTypeName(String crisisTypeName) {
        this.crisisTypeName = crisisTypeName;
    }

    public boolean isHasTaggerOutput() {
        return hasTaggerOutput;
    }

    public void setHasTaggerOutput(boolean hasTaggerOutput) {
        this.hasTaggerOutput = hasTaggerOutput;
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public Integer getClassifiersNumber() {
        return classifiersNumber;
    }

    public void setClassifiersNumber(Integer classifiersNumber) {
        this.classifiersNumber = classifiersNumber;
    }

    public String getGeoR() {
        return geoR;
    }

    public void setGeoR(String geoR) {
        this.geoR = geoR;
    }

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public int getFetchInterval() {
		return fetchInterval;
	}

	public void setFetchInterval(int fetchInterval) {
		this.fetchInterval = fetchInterval;
	}

	public int getFetchFrom() {
		return fetchFrom;
	}

	public void setFetchFrom(int fetchFrom) {
		this.fetchFrom = fetchFrom;
	}

	public long getTotalImageCount() {
		return totalImageCount;
	}

	public void setTotalImageCount(long totalImageCount) {
		this.totalImageCount = totalImageCount;
	}
}
