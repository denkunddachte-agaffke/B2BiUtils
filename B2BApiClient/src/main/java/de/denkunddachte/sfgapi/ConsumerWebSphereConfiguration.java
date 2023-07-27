/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.sfgapi;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ConsumerWebSphereConfiguration {
	public enum ChecksumMethod {
		MD5, none
	}

	public enum DestinationType {
		FILE, FILE_SPACE, PARTITIONED_DATA_SET, SEQUENTIAL_DATA_SET
	}

	public enum CommandType {
		ANTSCRIPT, EXECUTABLE, JCAL, NONE
	}

	public enum OverwriteMethod {
		ERROR, OVERWRITE
	}

	public enum TransferMode {
		BINARY, TEXT
	}

	private String			destinationAgentName;
	private String			destinationAgentQueueManager;
	private ChecksumMethod	destinationChecksumMethod;
	private String			destinationDirSetOrSpace;
	private OverwriteMethod	destinationFileAlreadyExists;
	private DestinationType	destinationType;
	private String			postAntProperties;
	private String			postAntTargets;
	private String			postCommand;
	private CommandType		postCommandType;
	private String			postExecArgs;
	private int				postRetryCount;
	private int				postRetryWait;
	private String			postReturnCode;
	private String			preAntProperties;
	private String			preAntTargets;
	private String			preCommand;
	private CommandType		preCommandType;
	private String			preExecArgs;
	private int				preRetryCount;
	private int				preRetryWait;
	private String			preReturnCode;
	private int				priority;
	private String			replyQueue;
	private String			queueManager;
	private String			sourceAgentAdapter;
	private TransferMode	transferMode;
	private String			jobName;
	private String			metadata;

	public ConsumerWebSphereConfiguration() {
		super();
	}

	public ConsumerWebSphereConfiguration(final JSONObject json) throws JSONException {
		super();
		this.destinationAgentName = json.getString("destinationAgentName");
		this.destinationAgentQueueManager = json.getString("destinationAgentQueueManager");
		this.destinationChecksumMethod = ChecksumMethod
				.valueOf(json.getJSONObject("destinationChecksumMethod").getString("code"));
		this.destinationDirSetOrSpace = json.getString("destinationDirSetOrSpace");
		this.destinationFileAlreadyExists = OverwriteMethod
				.valueOf(json.getJSONObject("destinationFileAlreadyExists").getString("code"));
		this.destinationType = DestinationType.valueOf(json.getJSONObject("destinationType").getString("code"));
		this.postAntProperties = json.getString("postAntProperties");
		this.postAntTargets = json.getString("postAntTargets");
		this.postCommand = json.getString("postCommand");
		this.postCommandType = CommandType.valueOf(json.getJSONObject("postCommandType").getString("code"));
		this.postExecArgs = json.getString("postExecArgs");
		this.postRetryCount = json.getInt("postRetryCount");
		this.postRetryWait = json.getInt("postRetryWait");
		this.postReturnCode = json.getString("postReturnCode");
		this.preAntProperties = json.getString("preAntProperties");
		this.preAntTargets = json.getString("preAntTargets");
		this.preCommand = json.getString("preCommand");
		this.preCommandType = CommandType.valueOf(json.getJSONObject("preCommandType").getString("code"));
		this.preExecArgs = json.getString("preExecArgs");
		this.preRetryCount = json.getInt("preRetryCount");
		this.preRetryWait = json.getInt("preRetryWait");
		this.preReturnCode = json.getString("preReturnCode");
		this.priority = json.getInt("priority");
		this.replyQueue = json.getString("replyQueue");
		this.queueManager = json.getString("queueManager");
		this.sourceAgentAdapter = json.getString("sourceAgentAdapter");
		this.transferMode = TransferMode.valueOf(json.getJSONObject("transferMode").getString("code"));
		this.jobName = json.getString("jobName");
		this.metadata = json.getString("metadata");
	}

	public String getDestinationAgentName() {
		return destinationAgentName;
	}

	public void setDestinationAgentName(String destinationAgentName) {
		this.destinationAgentName = destinationAgentName;
	}

	public String getDestinationAgentQueueManager() {
		return destinationAgentQueueManager;
	}

	public void setDestinationAgentQueueManager(String destinationAgentQueueManager) {
		this.destinationAgentQueueManager = destinationAgentQueueManager;
	}

	public ChecksumMethod getDestinationChecksumMethod() {
		return destinationChecksumMethod;
	}

	public void setDestinationChecksumMethod(ChecksumMethod destinationChecksumMethod) {
		this.destinationChecksumMethod = destinationChecksumMethod;
	}

	public String getDestinationDirSetOrSpace() {
		return destinationDirSetOrSpace;
	}

	public void setDestinationDirSetOrSpace(String destinationDirSetOrSpace) {
		this.destinationDirSetOrSpace = destinationDirSetOrSpace;
	}

	public OverwriteMethod getDestinationFileAlreadyExists() {
		return destinationFileAlreadyExists;
	}

	public void setDestinationFileAlreadyExists(OverwriteMethod destinationFileAlreadyExists) {
		this.destinationFileAlreadyExists = destinationFileAlreadyExists;
	}

	public DestinationType getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(DestinationType destinationType) {
		this.destinationType = destinationType;
	}

	public String getPostAntProperties() {
		return postAntProperties;
	}

	public void setPostAntProperties(String postAntProperties) {
		this.postAntProperties = postAntProperties;
	}

	public String getPostAntTargets() {
		return postAntTargets;
	}

	public void setPostAntTargets(String postAntTargets) {
		this.postAntTargets = postAntTargets;
	}

	public String getPostCommand() {
		return postCommand;
	}

	public void setPostCommand(String postCommand) {
		this.postCommand = postCommand;
	}

	public CommandType getPostCommandType() {
		return postCommandType;
	}

	public void setPostCommandType(CommandType postCommandType) {
		this.postCommandType = postCommandType;
	}

	public String getPostExecArgs() {
		return postExecArgs;
	}

	public void setPostExecArgs(String postExecArgs) {
		this.postExecArgs = postExecArgs;
	}

	public int getPostRetryCount() {
		return postRetryCount;
	}

	public void setPostRetryCount(int postRetryCount) {
		this.postRetryCount = postRetryCount;
	}

	public int getPostRetryWait() {
		return postRetryWait;
	}

	public void setPostRetryWait(int postRetryWait) {
		this.postRetryWait = postRetryWait;
	}

	public String getPostReturnCode() {
		return postReturnCode;
	}

	public void setPostReturnCode(String postReturnCode) {
		this.postReturnCode = postReturnCode;
	}

	public String getPreAntProperties() {
		return preAntProperties;
	}

	public void setPreAntProperties(String preAntProperties) {
		this.preAntProperties = preAntProperties;
	}

	public String getPreAntTargets() {
		return preAntTargets;
	}

	public void setPreAntTargets(String preAntTargets) {
		this.preAntTargets = preAntTargets;
	}

	public String getPreCommand() {
		return preCommand;
	}

	public void setPreCommand(String preCommand) {
		this.preCommand = preCommand;
	}

	public CommandType getPreCommandType() {
		return preCommandType;
	}

	public void setPreCommandType(CommandType preCommandType) {
		this.preCommandType = preCommandType;
	}

	public String getPreExecArgs() {
		return preExecArgs;
	}

	public void setPreExecArgs(String preExecArgs) {
		this.preExecArgs = preExecArgs;
	}

	public int getPreRetryCount() {
		return preRetryCount;
	}

	public void setPreRetryCount(int preRetryCount) {
		this.preRetryCount = preRetryCount;
	}

	public int getPreRetryWait() {
		return preRetryWait;
	}

	public void setPreRetryWait(int preRetryWait) {
		this.preRetryWait = preRetryWait;
	}

	public String getPreReturnCode() {
		return preReturnCode;
	}

	public void setPreReturnCode(String preReturnCode) {
		this.preReturnCode = preReturnCode;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getReplyQueue() {
		return replyQueue;
	}

	public void setReplyQueue(String replyQueue) {
		this.replyQueue = replyQueue;
	}

	public String getQueueManager() {
		return queueManager;
	}

	public void setQueueManager(String queueManager) {
		this.queueManager = queueManager;
	}

	public String getSourceAgentAdapter() {
		return sourceAgentAdapter;
	}

	public void setSourceAgentAdapter(String sourceAgentAdapter) {
		this.sourceAgentAdapter = sourceAgentAdapter;
	}

	public TransferMode getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(TransferMode transferMode) {
		this.transferMode = transferMode;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("destinationAgentName", destinationAgentName);
		json.put("destinationAgentQueueManager", destinationAgentQueueManager);
		json.put("destinationChecksumMethod", destinationChecksumMethod);
		json.put("destinationDirSetOrSpace", destinationDirSetOrSpace);
		json.put("destinationFileAlreadyExists", destinationFileAlreadyExists);
		json.put("destinationType", destinationType);
		json.put("jobName", jobName);
		json.put("metadata", metadata);
		json.put("postAntProperties", postAntProperties);
		json.put("postAntTargets", postAntTargets);
		json.put("postCommand", postCommand);
		json.put("postCommandType", postCommandType);
		json.put("postExecArgs", postExecArgs);
		json.put("postRetryCount", postRetryCount);
		json.put("postRetryWait", postRetryWait);
		json.put("postReturnCode", postReturnCode);
		json.put("preAntProperties", preAntProperties);
		json.put("preAntTargets", preAntTargets);
		json.put("preCommand", preCommand);
		json.put("preCommandType", preCommandType);
		json.put("preExecArgs", preExecArgs);
		json.put("preRetryCount", preRetryCount);
		json.put("preRetryWait", preRetryWait);
		json.put("preReturnCode", preReturnCode);
		json.put("priority", priority);
		json.put("queueManager", queueManager);
		json.put("replyQueue", replyQueue);
		json.put("sourceAgentAdapter", sourceAgentAdapter);
		json.put("transferMode", transferMode);
		return json;
	}

	@Override
	public String toString() {
		return "ConsumerWebSphereConfiguration [destinationAgentName=" + destinationAgentName + ", destinationAgentQueueManager="
				+ destinationAgentQueueManager + ", destinationChecksumMethod=" + destinationChecksumMethod
				+ ", destinationDirSetOrSpace=" + destinationDirSetOrSpace + ", destinationFileAlreadyExists="
				+ destinationFileAlreadyExists + ", destinationType=" + destinationType + ", postAntProperties="
				+ postAntProperties + ", postAntTargets=" + postAntTargets + ", postCommand=" + postCommand + ", postCommandType="
				+ postCommandType + ", postExecArgs=" + postExecArgs + ", postRetryCount=" + postRetryCount + ", postRetryWait="
				+ postRetryWait + ", postReturnCode=" + postReturnCode + ", preAntProperties=" + preAntProperties
				+ ", preAntTargets=" + preAntTargets + ", preCommand=" + preCommand + ", preCommandType=" + preCommandType
				+ ", preExecArgs=" + preExecArgs + ", preRetryCount=" + preRetryCount + ", preRetryWait=" + preRetryWait
				+ ", preReturnCode=" + preReturnCode + ", priority=" + priority + ", replyQueue=" + replyQueue + ", queueManager="
				+ queueManager + ", sourceAgentAdapter=" + sourceAgentAdapter + ", transferMode=" + transferMode + ", jobName="
				+ jobName + ", metadata=" + metadata + "]";
	}

}
