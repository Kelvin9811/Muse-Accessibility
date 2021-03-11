jsonFileLongBlink = 'LongBlink.json';
jsonFileShortBlink = 'ShortBlink.json';

jsonDataLongBlink = jsondecode(fileread(jsonFileLongBlink));
jsonDataShortBlink = jsondecode(fileread(jsonFileShortBlink));


originalSignalLongBlink = jsonDataLongBlink.data(:,4);
originalSignalShortBlink = jsonDataShortBlink.data(:,4);

numSamplesPerSecond= 256;
sampleLength = 1020;
fc = 3;
fs = numSamplesPerSecond; 
orderOfTheFilter = 5; 
[b,a] = butter(orderOfTheFilter,fc/(fs/2));

absoluteSignalLongBlink=abs(originalSignalLongBlink - mean(originalSignalLongBlink));
absoluteSignalShortBlink=abs(originalSignalShortBlink - mean(originalSignalShortBlink));

filteredSignalLongBlink = filter(b,a,absoluteSignalLongBlink);
filteredSignalShortBlink = filter(b,a,absoluteSignalShortBlink);

%2-3-4-5 para entrenamiento 6-20
sampleNumber=10;

trainingSampleStart= (sampleNumber*sampleLength)-sampleLength;
trainingSampleEnd= sampleNumber*sampleLength;

trainingShortBlink = filteredSignalShortBlink(trainingSampleStart:trainingSampleEnd);
trainingShortBlink =(trainingShortBlink - min(trainingShortBlink ))/(max(trainingShortBlink )-min(trainingShortBlink ));
plot(trainingShortBlink)

trainingLongtBlink = filteredSignalLongBlink(trainingSampleStart:trainingSampleEnd);
trainingLongtBlink =(trainingShortBlink - min(trainingShortBlink ))/(max(trainingShortBlink )-min(trainingShortBlink ));

distanceSameSample = dtw(trainingShortBlink,trainingShortBlink);

%samples numbers 6:19
%zero = short blink

for i = 1:5 
    dbSampleStart= ((i+5)*sampleLength)-sampleLength;
    dbSampleEnd= (i+5)*sampleLength;
    
    dbShortBlink = filteredSignalShortBlink(dbSampleStart:dbSampleEnd);
    dbShortBlink =(dbShortBlink - min(dbShortBlink ))/(max(dbShortBlink )-min(dbShortBlink ));
    
    distanceShortBlink(1,i) = dtw(trainingShortBlink,dbShortBlink);    
    distanceShortBlink(2,i) = 0;

end

%samples numbers 6:19
%one = long blink

for i = 1:5 
    dbSampleStart= ((i+5)*sampleLength)-sampleLength;
    dbSampleEnd= (i+5)*sampleLength;
    
    dbLongBlink = filteredSignalLongBlink(dbSampleStart:dbSampleEnd);
    dbLongBlink =(dbLongBlink - min(dbLongBlink ))/(max(dbLongBlink )-min(dbLongBlink ));
    
    distanceShortBlink(1,i+5) = dtw(trainingShortBlink,dbLongBlink);
    distanceShortBlink(2,i+5) = 1;
end


for j = 0 : 9
    
    for i = 1: 9
        
        if distanceShortBlink(1,i)>distanceShortBlink(1,i+1)
            
            temp = distanceShortBlink(1,i);
            distanceShortBlink(1,i) = distanceShortBlink(1,i+1);
            distanceShortBlink(1,i+1) = temp;
            distanceShortBlink(1,i+1) = temp;
            
            temp = distanceShortBlink(2,i);
            distanceShortBlink(2,i) = distanceShortBlink(2,i+1);
            distanceShortBlink(2,i+1) = temp;
            distanceShortBlink(2,i+1) = temp;

        end       
    end
end

k = 5
numberOfShortBlinks = 0;
numberOfLongBlinks = 0;

for i = 1: k
   
    if (distanceShortBlink(2,i) == 0)
        numberOfShortBlinks = numberOfShortBlinks+1;
    end  
    if (distanceShortBlink(2,i) == 1)
        numberOfLongBlinks = numberOfLongBlinks+1;
    end 
end

numberOfShortBlinks = numberOfShortBlinks/k
numberOfLongBlinks = numberOfLongBlinks/k


