% TSNE vamos ausar para pasar una matriz de V[50][1020]
% a un vector de V[50][2]
%Columna 1 = Timestamp, Columna 2,3,4 y 5 = Canal 1,2,3 y 4

osOneShortBlink  = jsondecode(fileread('ShortBlink.json')).data(:,4);
osOneLongBlink  = jsondecode(fileread('LongBlink.json')).data(:,4);

osTwoShortBlink  = jsondecode(fileread('ShortBlink.json')).data(:,4);
osTwoLongBlink  = jsondecode(fileread('LongBlink.json')).data(:,4);

osThreeShortBlink  = jsondecode(fileread('ShortBlink.json')).data(:,4);
osThreeLongBlink  = jsondecode(fileread('LongBlink.json')).data(:,4);

osFourShortBlink = jsondecode(fileread('ShortBlink.json')).data(:,4);
osFourLongBlink  = jsondecode(fileread('LongBlink.json')).data(:,4);

osFiveShortBlink  = jsondecode(fileread('ShortBlink.json')).data(:,4);
osFiveLongBlink  = jsondecode(fileread('LongBlink.json')).data(:,4);

osShortBlink= [osOneShortBlink ;osTwoShortBlink ;osThreeShortBlink ;osFourShortBlink ;osFiveShortBlink];
osLongBlink = [osOneLongBlink ;osTwoLongBlink  ;osThreeLongBlink; osFourLongBlink ;osFiveLongBlink];

psShortBlink=signalProcessing(osShortBlink);
psLongBlink=signalProcessing(osLongBlink);

blinkTypes = cell(500,1);
blinkTypes(1:250) = {'cortos'};
blinkTypes(251:500) = {'largos'};

sampleCount = 1;
sampleLength = 510;

sampleToTransform = zeros(500,sampleLength);

for i= 1:250  
    for j= 1:sampleLength         
        sampleToTransform(i,j)=psShortBlink(sampleCount);
        sampleCount=sampleCount+1;
    end   
end

sampleCount = 1;

for i= 251:500   
    for j= 1:sampleLength         
        sampleToTransform(i,j)=psLongBlink(sampleCount);
        sampleCount=sampleCount+1;
    end   
end




Y = tsne(sampleToTransform);
gscatter(Y(:,1),Y(:,2),blinkTypes)

