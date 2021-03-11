jsonFile = 'ShortBlink.json';
jsonData = jsondecode(fileread(jsonFile));

data = jsonData.data;

chanelOne=abs(data(:,2));



numSamplesPerSecond = 256; % número de muetsras por segundo (frecuencia de muestreo)
signal =chanelOne;
% Creación de un filtro digital pasabajos de Butterworth
fc = 1.5; % frecuencia de corte del filtro en Hz (fc <= fs/2)
fs = numSamplesPerSecond; %Frecuencia de muestreo 
orderOfTheFilter = 5; % orden del filtro
[b,a] = butter(orderOfTheFilter,fc/(fs/2)); % Creación del filtro

% Aplicación del filtro
filteredSignal = filter(b,a,signal);

chanelOne=filteredSignal;



sampleLength = 1020;
numberSamples=3;

sampleToTransform = zeros(3,sampleLength);

%Toma aleatoria de muestras sobrepuestas
   subplot(4,1,1);

    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    
    hold on 
    plot(chanelOne(sampleStart:sampleEnd))
    hold off

    
	hold on 
    subplot(4,1,1);
    plot(chanelOne(sampleStart:sampleEnd))
    hold off
    
    
     subplot(4,1,2);

    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    
    plot(chanelOne(sampleStart:sampleEnd))
   
    
    
     subplot(4,1,3);

    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    
    plot(chanelOne(sampleStart:sampleEnd))
   
    
    
     subplot(4,1,4);

    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    
    plot(chanelOne(sampleStart:sampleEnd))
   