clc;
close all;
clear all;

% Simulación de una señal donde las filas son los instantes de tiemoo y las
% columnas son los canales
filename = 'Raw_EEG1.csv';
X = csvread(filename,0,2,[0,2,1039,2]);

numChannels = 1; % número de canales
numSamplesPerSecond = 256; % número de muetsras por segundo (frecuencia de muestreo)
timeOfMeasure = 1; % Tiempo de medición
% signal = 10*randn(numSamplesPerSecond*timeOfMeasure, numChannels); 
signal =X;
% Creación de un filtro digital pasabajos de Butterworth
fc = 15; % frecuencia de corte del filtro en Hz (fc <= fs/2)
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
