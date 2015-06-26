grep '@' int_sampler.log >1.txt
awk 'BEGIN{FS=OFS="@"} NF>1{$1="";sub(/^@*/, "")}'1 1.txt >int_sampler.log
