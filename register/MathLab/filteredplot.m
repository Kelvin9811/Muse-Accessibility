function RawEEG2 = importfile( dataLines)
filename = 'Extratedfiltered.csv';
if nargin < 2
    dataLines = [1, Inf];
end

opts = delimitedTextImportOptions("NumVariables", 2);

opts.DataLines = dataLines;
opts.Delimiter = ",";

opts.VariableNames = ["VarName1", "E120005269582846581417","VarName3"];
opts.VariableTypes = ["double", "double", "double"];

opts.ExtraColumnsRule = "ignore";
opts.EmptyLineRule = "read";

RawEEG2 = readtable(filename, opts);




signal =RawEEG2.VarName3;

plot(signal);
title('señal original');

end