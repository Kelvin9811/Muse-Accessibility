% TSNE vamos ausar para pasar una matriz de V[50][1020]
% a un vector de V[50][2]

jsonFileLongBlink = 'LongBlink.json';
jsonDataLongBlink  = jsondecode(fileread(jsonFileLongBlink));

%Columna 1 = Timestamp, Columna 2,3,4 y 5 = Canal 1,2,3 y 4
chanelOneLongBlink=jsonDataLongBlink.data(:,2);

sampleLength = 1020;

sampleToTransform = zeros(100,sampleLength);

sampleCount = 1;

for i= 1:50  
    for j= 1:sampleLength         
        sampleToTransform(i,j)=chanelOneLongBlink(sampleCount);
        sampleCount=sampleCount+1;
    end   
end

Y = tsne(sampleToTransform);
gscatter(Y(:,1),Y(:,2))