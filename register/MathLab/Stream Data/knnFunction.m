function knnAns = knnFunction(sampleToEvaluate)
jsonFileLongBlink = 'LongBlinkDB.json';
jsonFileShortBlink = 'ShortBlinkDB.json';
jsonFileNoneBlink = 'NoneBlinkDB.json';

originalSignalShortBlink = jsondecode(fileread(jsonFileShortBlink)).data(:,2);
originalSignalLongBlink = jsondecode(fileread(jsonFileLongBlink)).data(:,2);
originalSignalNoneBlink = jsondecode(fileread(jsonFileNoneBlink)).data(:,2);

psShortBlink = (originalSignalShortBlink);
psLongBlink =  (originalSignalLongBlink);
psNonegBlink = (originalSignalNoneBlink);

distancesBlink(2,15)=zeros;

for i = 1:15     
    
    dbShortBlink =getSample(psShortBlink,i);
    
    distancesBlink(1,i) = dtw(sampleToEvaluate,dbShortBlink);    
    distancesBlink(2,i) = 0;
    
    dbLongBlink =getSample(psLongBlink,i);
    
    distancesBlink(1,i+15) = dtw(sampleToEvaluate,dbLongBlink);
    distancesBlink(2,i+15) = 1;
   
    dbNoneBlink =getSample(psNonegBlink,i);
    
    distancesBlink(1,i+30) = dtw(sampleToEvaluate,dbNoneBlink);
    distancesBlink(2,i+30) = 2;
end


for j = 1 : 44
    
    for i = 1: 44
        
        if distancesBlink(1,i)>distancesBlink(1,i+1)
            
            temp = distancesBlink(1,i);
            distancesBlink(1,i) = distancesBlink(1,i+1);
            distancesBlink(1,i+1) = temp;
            distancesBlink(1,i+1) = temp;
            
            temp = distancesBlink(2,i);
            distancesBlink(2,i) = distancesBlink(2,i+1);
            distancesBlink(2,i+1) = temp;
            distancesBlink(2,i+1) = temp;

        end       
    end
end

k = 10;

numberOfShortBlinks = 0;
numberOfLongBlinks = 0;
numberOfNoneBlinks = 0;

for i = 1: k
   
    if (distancesBlink(2,i) == 0)
        numberOfShortBlinks = numberOfShortBlinks+1;
    end  
    if (distancesBlink(2,i) == 1)
        numberOfLongBlinks = numberOfLongBlinks+1;
    end 
    
    if (distancesBlink(2,i) == 2)
        numberOfNoneBlinks = numberOfNoneBlinks+1;
    end 
    
    
end

shortBlinkProbability = numberOfShortBlinks/k
longBlinkProbability = numberOfLongBlinks/k
noneBlinkProbability = numberOfNoneBlinks/k

end
