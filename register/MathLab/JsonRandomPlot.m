jsonFile = 'ShortBlink.json';
jsonData = jsondecode(fileread(jsonFile));

data = jsonData.data;

chanelOne=data(:,2);

sampleLength = 1020;

sampleToTransform = zeros(3,sampleLength);


%Toma aleatoria de muestras sobrepuestas

for i= 1:7
    hold on
    rand = randi([1 50])

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    
    plot(chanelOne(sampleStart:sampleEnd))
    hold off
end


% TSNE vamos ausar para pasar una matriz de V[50][1020]
% a un vector de V[50][2]
%{
for i= 1:50   
    sampleStart= (i*sampleLength)-sampleLength+1;
    sampleEnd= i*sampleLength;
    for j= 1:sampleLength         
        sampleToTransform(i,j)=chanelOne(j);
    end   
end
Y = tsne(sampleToTransform);
gscatter(Y(:,1),Y(:,2))
%}




