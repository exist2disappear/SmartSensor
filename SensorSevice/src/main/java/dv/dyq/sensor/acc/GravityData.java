package dv.dyq.sensor.acc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class GravityData {
	
	private float [] rawDatainX=new float[3000];
	private float [] rawDatainY=new float[3000];
	private float [] rawDatainZ=new float[3000];
	private int [] time=new int[3000];
	public int poitGetinTest;
	
	public GravityData() {
		poitGetinTest=0;
	}
	
	private void midlleValueFilter(int windowlong){}
	
	//get the meandata,minTime and max time mustnot the edge
	private float getMeanData(float [] signal,int minTime,int maxTime){
		float signalMean=0;
		float [] signalTemp=new float[maxTime-minTime+1];
		int timeWriteIn=minTime;
		for(int point=0;point<poitGetinTest;point++){
			if(time[point]>minTime&&time[point]<maxTime){
				if(time[point]==timeWriteIn){
					signalTemp[timeWriteIn-minTime]=signal[point];
					timeWriteIn++;
				}
				else{
					while(time[point]>timeWriteIn){
						signalTemp[timeWriteIn-minTime]=signal[point-1];
						timeWriteIn++;
					}
					signalTemp[timeWriteIn-minTime]=signal[point];
					timeWriteIn++;
				}
			}		
		}
		while (timeWriteIn<=maxTime){
			signalTemp[timeWriteIn-minTime]=signalTemp[timeWriteIn-minTime-1];
			timeWriteIn++;
		}
		Arrays.sort(signalTemp);
		signalMean=signalTemp[(signalTemp.length/2)];
		return signalMean;
	}
	///
	private void getDeGenerate(){
		int pointNumDeGenerate=0;
		int timeLast=time[0];
		int sameTimeNum=1;
		for(int pointNum=1;pointNum<poitGetinTest;pointNum++)
		{
			if(timeLast!=time[pointNum])
			{
				time[pointNumDeGenerate]=timeLast;
				timeLast=time[pointNum];
				rawDatainX[pointNumDeGenerate]=(rawDatainX[pointNum-1]+rawDatainX[pointNum-sameTimeNum])/2;
				rawDatainY[pointNumDeGenerate]=(rawDatainY[pointNum-1]+rawDatainY[pointNum-sameTimeNum])/2;
				rawDatainZ[pointNumDeGenerate]=(rawDatainZ[pointNum-1]+rawDatainZ[pointNum-sameTimeNum])/2;
				pointNumDeGenerate++;
				sameTimeNum=1;
			}
			else
				sameTimeNum++;
		};
		poitGetinTest=pointNumDeGenerate;
		
	}
	
	private ArrayGap getSignificantPart(float attentionRate,float[] dataInaixs){
		ArrayGap significantData=new ArrayGap(0,poitGetinTest);
		float maxAbsofValue=0;
		int stayinHigh=0;
		int STAYLONG=5;
		for(int point=0;point<poitGetinTest;point++)
		{
			if(Math.abs(dataInaixs[point])>maxAbsofValue)
				maxAbsofValue=Math.abs(dataInaixs[point]);
		}
		for(int point=1;point<poitGetinTest;point++)
		{
			if(stayinHigh>STAYLONG)
				break;
			else if(Math.abs(dataInaixs[point])>maxAbsofValue*attentionRate)
				stayinHigh+=time[point]-time[point-1];
			else{
				stayinHigh=0;
				significantData.startOfData=point;
			}		
		}
		stayinHigh=0;///count for long back to "0"
		for(int point=poitGetinTest-2;point>1;point--)
		{
			if(stayinHigh>STAYLONG)
				break;
			else if(Math.abs(dataInaixs[point])>maxAbsofValue*attentionRate)
				stayinHigh+=time[point+1]-time[point];
			else{
				stayinHigh=0;
				significantData.endOfData=point;
			}		
		}
		
		return significantData;
	}
	
	private void sumTimepointData(float[] dataInaixs,int startPoint,int endPoint){
		dataInaixs[startPoint]=0;//init data is zero
		for(int point=startPoint+1;point<endPoint;point++){
			dataInaixs[point]=dataInaixs[point-1]+dataInaixs[point]*(time[point]-time[point-1])*0.02f;
		}
		
	}
	//rule the length of x direction 
	public float getLongX(){
		float totalLong=0;
		/*--------------frist time adjust acc-----------------*/
		float startAccValue=getMeanData(rawDatainX,time[1]+3,time[1]+18);///push button is not statable
		float endofAccValue=getMeanData(rawDatainX,time[poitGetinTest-1]-18,time[poitGetinTest-1]-3);
		float accChangeRate=(startAccValue-endofAccValue)/(time[poitGetinTest-1]-10);
		float speedChangeRate=0;
		for(int point=0;point<poitGetinTest;point++){
			rawDatainX[point]=rawDatainX[point]+accChangeRate*time[point]-startAccValue;
		}
		/*--------------get signifigent part in signal-----------------*/
		ArrayGap dataRange = getSignificantPart(0.1f,rawDatainX);
		if(dataRange.startOfData<dataRange.endOfData){
			/*--------------change to speed-----------------*/
			sumTimepointData(rawDatainX,dataRange.startOfData,dataRange.endOfData);
			/*--------------second time adjust speed-----------------*/
			speedChangeRate=-rawDatainX[dataRange.endOfData-1]/(time[dataRange.endOfData-1]-time[dataRange.startOfData]);
			for(int point=dataRange.startOfData;point<dataRange.endOfData;point++){
				rawDatainX[point]=rawDatainX[point]+speedChangeRate*(time[point]-time[dataRange.startOfData]);
			}
			/*--------------change to length-----------------*/
			sumTimepointData(rawDatainX,dataRange.startOfData,dataRange.endOfData);
		}
		totalLong=rawDatainX[dataRange.endOfData-1];
		return totalLong;
	}
	///rule Y
	public float getLongY(){
		float totalLong=0;
		/*--------------frist time adjust acc-----------------*/
		float startAccValue=getMeanData(rawDatainY,time[1]+3,time[1]+18);///push button is not statable
		float endofAccValue=getMeanData(rawDatainY,time[poitGetinTest-1]-18,time[poitGetinTest-1]-3);
		float accChangeRate=(startAccValue-endofAccValue)/(time[poitGetinTest-1]-10);
		float speedChangeRate=0;
		for(int point=0;point<poitGetinTest;point++){
			rawDatainY[point]=rawDatainY[point]+accChangeRate*time[point]-startAccValue;
		}
		/*--------------get signifigent part in signal-----------------*/
		ArrayGap dataRange = getSignificantPart(0.1f,rawDatainY);
		if(dataRange.startOfData<dataRange.endOfData){
			/*--------------change to speed-----------------*/
			sumTimepointData(rawDatainY,dataRange.startOfData,dataRange.endOfData);
			/*--------------second time adjust speed-----------------*/
			speedChangeRate=-rawDatainY[dataRange.endOfData-1]/(time[dataRange.endOfData-1]-time[dataRange.startOfData]);
			for(int point=dataRange.startOfData;point<dataRange.endOfData;point++){
				rawDatainY[point]=rawDatainY[point]+speedChangeRate*(time[point]-time[dataRange.startOfData]);
			}
			/*--------------change to length-----------------*/
			sumTimepointData(rawDatainY,dataRange.startOfData,dataRange.endOfData);
		}
		totalLong=rawDatainY[dataRange.endOfData-1];
		return totalLong;
	}
	//rule Z
	public float getLongZ(){
		float totalLong=0;
		/*--------------frist time adjust acc-----------------*/
		float startAccValue=getMeanData(rawDatainZ,time[1]+3,time[1]+18);///push button is not statable
		float endofAccValue=getMeanData(rawDatainZ,time[poitGetinTest-1]-18,time[poitGetinTest-1]-3);
		float accChangeRate=(startAccValue-endofAccValue)/(time[poitGetinTest-1]-10);
		float speedChangeRate=0;
		for(int point=0;point<poitGetinTest;point++){
			rawDatainZ[point]=rawDatainZ[point]+accChangeRate*time[point]-startAccValue;
		}
		/*--------------get signifigent part in signal-----------------*/
		ArrayGap dataRange = getSignificantPart(0.1f,rawDatainZ);
		if(dataRange.startOfData<dataRange.endOfData){
			/*--------------change to speed-----------------*/
			sumTimepointData(rawDatainZ,dataRange.startOfData,dataRange.endOfData);
			/*--------------second time adjust speed-----------------*/
			speedChangeRate=-rawDatainZ[dataRange.endOfData-1]/(time[dataRange.endOfData-1]-time[dataRange.startOfData]);
			for(int point=dataRange.startOfData;point<dataRange.endOfData;point++){
				rawDatainZ[point]=rawDatainZ[point]+speedChangeRate*(time[point]-time[dataRange.startOfData]);
			}
			/*--------------change to length-----------------*/
			sumTimepointData(rawDatainZ,dataRange.startOfData,dataRange.endOfData);
		}
		totalLong=rawDatainZ[dataRange.endOfData-1];
		return totalLong;
	}
	
	
	///initalize the test item before test
	public void initDataBeforeTest(){
		poitGetinTest=0;
	}
	///get the real-time data in accelerate sensor
	public  boolean popData(float accx,float accy,float accz,int cntTime){
		if(poitGetinTest<3000)
		{
			rawDatainX[poitGetinTest]=accx;
			rawDatainY[poitGetinTest]=accy;
			rawDatainZ[poitGetinTest]=accz;
			time[poitGetinTest]=cntTime;
			poitGetinTest++;
			return true;
		}
		else
			return false;
	}
	///save the data for pc matlab test
	public String saveData(String datafilename){
		int writeThis;
		float fBigSenser=0;
		int iBigSenser=0;
		String log="";
		File file=new File(datafilename);
		

		try {
			DataOutputStream out =new DataOutputStream(new
					FileOutputStream(file));
			//FileOutputStream out=new FileOutputStream(file);//unkownword
			//RandomAccessFile out=new RandomAccessFile(file,"rw");
			out.writeShort(poitGetinTest);
			for(writeThis=0;writeThis<poitGetinTest;writeThis++)
				try {
					
					//out.writeFloat(acc_line_x[writeThis]);
					//float fa=184.6f;
					fBigSenser=100*rawDatainX[writeThis];
					iBigSenser=(int)fBigSenser;
					out.writeShort(iBigSenser);
					fBigSenser=100*rawDatainY[writeThis];
					iBigSenser=(int)fBigSenser;
					out.writeShort(iBigSenser);
					fBigSenser=100*rawDatainZ[writeThis];
					iBigSenser=(int)fBigSenser;
					out.writeShort(iBigSenser);
					iBigSenser=(int)time[writeThis];
					out.writeShort(iBigSenser);
					log="write all rihgt";
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log="write data error";
				}
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log="close file error";
			}
			} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
				log="file generate error";
		} catch (IOException e1) {
				// TODO Auto-generated catch block
			log="file head write error";
			}
		return log;
		
	}
	
}
