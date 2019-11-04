package com.amithkk.breathsampler.pojo;

public class BreathResult{
	private String result;
	private String audioURL;
	private int sampleLength;
	private long timeAdded;
	private String extra;
	private String status;

	public BreathResult(String result, String audioURL, int sampleLength, long timeAdded, String extra, String status) {
		this.result = result;
		this.audioURL = audioURL;
		this.sampleLength = sampleLength;
		this.timeAdded = timeAdded;
		this.extra = extra;
		this.status = status;
	}


	public BreathResult(String audioURL, int sampleLength, long timeAdded) {
		this.result = "All Okay";
		this.audioURL = audioURL;
		this.sampleLength = sampleLength;
		this.timeAdded = timeAdded;
		this.extra = "NullNullNull";
		this.status = "DONE";
	}


	public BreathResult() {
	}

	public void setResult(String result){
		this.result = result;
	}

	public String getResult(){
		return result;
	}

	public void setAudioURL(String audioURL){
		this.audioURL = audioURL;
	}

	public String getAudioURL(){
		return audioURL;
	}

	public void setSampleLength(int sampleLength){
		this.sampleLength = sampleLength;
	}

	public int getSampleLength(){
		return sampleLength;
	}

	public void setTimeAdded(long timeAdded){
		this.timeAdded = timeAdded;
	}

	public long getTimeAdded(){
		return timeAdded;
	}

	public void setExtra(String extra){
		this.extra = extra;
	}

	public String getExtra(){
		return extra;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"BreathResult{" + 
			"result = '" + result + '\'' + 
			",audioURL = '" + audioURL + '\'' + 
			",sampleLength = '" + sampleLength + '\'' + 
			",timeAdded = '" + timeAdded + '\'' + 
			",extra = '" + extra + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}
