function [b,a] = coefsFilter()

numSamplesPerSecond= 256;

%2-11 clasificación del 100%
fc = 2;

fs = numSamplesPerSecond; 

% 11 ya empeiza a fallar
orderOfTheFilter = 5; 

[b,a] = butter(orderOfTheFilter,fc/(fs/2));

end