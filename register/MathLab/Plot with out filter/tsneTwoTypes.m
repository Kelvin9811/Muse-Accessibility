% TSNE vamos ausar para pasar una matriz de V[50][1020]
% a un vector de V[50][2]

jsonFileShortBlink = 'ShortBlink.json';
jsonDataShortBlink  = jsondecode(fileread(jsonFileShortBlink));

jsonFileLongBlink = 'LongBlink.json';
jsonDataLongBlink  = jsondecode(fileread(jsonFileLongBlink));

%Columna 1 = Timestamp, Columna 2,3,4 y 5 = Canal 1,2,3 y 4

osShortBlink=jsonDataShortBlink.data(:,4);
osLongBlink=jsonDataLongBlink.data(:,4);

psShortBlink=signalProcessing(osShortBlink);
psLongBlink=signalProcessing(osLongBlink);

blinkTypes = cell(100,1);
blinkTypes(1:50) = {'cortos'};
blinkTypes(51:100) = {'largos'};

sampleToTransform = zeros(100,sampleLength);

sampleCount = 1;
sampleLength = 510;

for i= 1:50  
    for j= 1:sampleLength         
        sampleToTransform(i,j)=psShortBlink(sampleCount);
        sampleCount=sampleCount+1;
    end   
end

sampleCount = 1;

for i= 51:100   
    for j= 1:sampleLength         
        sampleToTransform(i,j)=psLongBlink(sampleCount);
        sampleCount=sampleCount+1;
    end   
end


Y = tsne(sampleToTransform);
gscatter(Y(:,1),Y(:,2),blinkTypes)

