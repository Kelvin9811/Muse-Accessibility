jsonFile = 'LongBlink.json';
jsonData = jsondecode(fileread(jsonFile));

data = jsonData.data;

chanelOne=abs(data(:,2));


numSamplesPerSecond = 256; % número de muetsras por segundo (frecuencia de muestreo)
signal =chanelOne;
% Creación de un filtro digital pasabajos de Butterworth
fc = 2; % frecuencia de corte del filtro en Hz (fc <= fs/2)
fs = numSamplesPerSecond; %Frecuencia de muestreo 
orderOfTheFilter = 5; % orden del filtro
[b,a] = butter(orderOfTheFilter,fc/(fs/2)); % Creación del filtro

% Aplicación del filtro
filteredSignal = filter(b,a,signal);

chanelOne=filteredSignal;



sampleLength = 1020;
numberSamples=2;

sampleToTransform = zeros(3,sampleLength);

%Toma aleatoria de muestras sobrepuestas


    
    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    subplot(4,1,1);
    plot(chanelOne(sampleStart:sampleEnd))
  
    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    subplot(4,1,2);
    plot(chanelOne(sampleStart:sampleEnd))
   

    
  
    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    subplot(4,1,3);
    plot(chanelOne(sampleStart:sampleEnd))
 
    rand = randi([1 50]);

    sampleStart= (rand*sampleLength)-sampleLength;
    sampleEnd= rand*sampleLength;
    subplot(4,1,4);
    plot(chanelOne(sampleStart:sampleEnd))
  
