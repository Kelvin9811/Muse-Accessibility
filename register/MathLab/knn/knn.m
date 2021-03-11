jsonFileLongBlink = 'LongBlink.json';
jsonFileShortBlink = 'ShortBlink.json';

originalSignalShortBlink = jsondecode(fileread(jsonFileShortBlink)).data(:,2);
originalSignalLongBlink = jsondecode(fileread(jsonFileLongBlink)).data(:,2);

%psShortBlink = signalProcessing(originalSignalShortBlink);
%psLongBlink =  signalProcessing(originalSignalLongBlink);

psShortBlink = originalSignalShortBlink;
psLongBlink = originalSignalLongBlink;

sampleNumber=randi([1 15]);

sampleToEvaluate = getSample(psShortBlink,sampleNumber);
%sampleToEvaluate = getSample(psLongBlink,sampleNumber);

plot(sampleToEvaluate)

%2-5 para tomar y comparar
%6-20 para entrenamiento 

distanceShortBlink(2,10)=zeros;

for i = 1:15     
    
    dbShortBlink =getSample(psShortBlink,i);
    
    distanceShortBlink(1,i) = dtw(sampleToEvaluate,dbShortBlink);    
    distanceShortBlink(2,i) = 0;
    
    dbLongBlink =getSample(psLongBlink,i);
    
    distanceShortBlink(1,i+15) = dtw(sampleToEvaluate,dbLongBlink);
    distanceShortBlink(2,i+15) = 1;
    
end


for j = 1 : 29
    
    for i = 1: 29
        
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

k = 15;

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

shortBlinkProbability = numberOfShortBlinks
longBlinkProbability = numberOfLongBlinks


