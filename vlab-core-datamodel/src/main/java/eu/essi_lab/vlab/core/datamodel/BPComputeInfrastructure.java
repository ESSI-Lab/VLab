package eu.essi_lab.vlab.core.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;

/**
 * @author Mattia Santoro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BPComputeInfrastructure {

	@JsonInclude
	private String prepareImage;

	@JsonInclude
	private String awsCliImage;

	@JsonInclude
	private String type;

	@JsonInclude
	private String label;

	@JsonInclude
	private String s3SecretKey;

	@JsonInclude
	private Optional<String> s3ServiceUrl = Optional.empty();

	@JsonInclude
	private String s3AccessKey;

	@JsonInclude
	private String s3BucketRegion;
	private String id;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("BPInfrastructure: ").append(getLabel()).append(" of type ").append(getType()).append(" [id: ").append(getId())
				.append("]");

		return builder.toString();
	}

	public String getS3SecretKey() {
		return s3SecretKey;
	}

	public void setS3SecretKey(String secretKey) {
		this.s3SecretKey = secretKey;
	}

	public String getS3BucketRegion() {
		return s3BucketRegion;
	}

	public void setS3BucketRegion(String s3BucketRegion) {
		this.s3BucketRegion = s3BucketRegion;
	}

	public String getS3AccessKey() {
		return s3AccessKey;
	}

	public void setS3AccessKey(String accessKey) {
		this.s3AccessKey = accessKey;
	}

	public Optional<String> getS3ServiceUrl() {
		return s3ServiceUrl;
	}

	public void setS3ServiceUrl(Optional<String> s3ServiceUrl) {
		this.s3ServiceUrl = s3ServiceUrl;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getAwsCliImage() {
		return awsCliImage;
	}

	public void setAwsCliImage(String awsCliImage) {
		this.awsCliImage = awsCliImage;
	}

	public String getPrepareImage() {
		return prepareImage;
	}

	public void setPrepareImage(String prepeareImage) {
		this.prepareImage = prepeareImage;
	}
}
