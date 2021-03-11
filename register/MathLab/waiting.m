function RawEEG2 = importfile( dataLines)
%15 segundos sin parpadear
filename = 'waiting.csv';

if nargin < 2
    dataLines = [1, Inf];
end

opts = delimitedTextImportOptions("NumVariables", 1);

opts.DataLines = dataLines;
opts.Delimiter = ";";

opts.VariableNames = ["VarName1"];
opts.VariableTypes = ["double"];

opts.ExtraColumnsRule = "ignore";
opts.EmptyLineRule = "read";

RawEEG2 = readtable(filename, opts);


numSamplesPerSecond = 256; % número de muetsras por segundo (frecuencia de muestreo)

signal =RawEEG2.VarName1;
% Creación de un filtro digital pasabajos de Butterworth
fc = 5; % frecuencia de corte del filtro en Hz (fc <= fs/2)
fs = numSamplesPerSecond; %Frecuencia de muestreo 
orderOfTheFilter = 5; % orden del filtro
[b,a] = butter(orderOfTheFilter,fc/(fs/2)); % Creación del filtro

% Aplicación del filtro
filteredSignal = filter(b,a,signal);

% Gráficos
figure;
subplot(2,1,1);
plot(signal);
title('señal original');

subplot(2,1,2);
plot(filteredSignal);
title('señal filtrada');


end