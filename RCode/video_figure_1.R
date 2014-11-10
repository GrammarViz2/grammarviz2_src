require(reshape)
require(scales)
#
require(Cairo)
require(ggplot2)
require(RColorBrewer)
require(grid)
require(gridExtra)
require(lattice)
#
# psenin@piedras:~/git/grammarviz2_src.git$ sed -n 1500,3500p data/ann_gun_CentroidA1.csv >anomaly_01.csv
# psenin@piedras:~/git/grammarviz2_src.git$ java -Xmx2G -cp "grammarviz20.jar" edu.hawaii.jmotif.discord.SAXSequiturDiscord 3 anomaly_01.csv 160 6 6 5
# 16:06:23.185 [main] INFO  e.h.j.discord.SAXSequiturDiscord - Parsing param string "[3, anomaly_01.csv, 160, 6, 6, 5]"
# 16:06:23.188 [main] INFO  e.h.j.discord.SAXSequiturDiscord - reading from anomaly_01.csv
# 16:06:23.223 [main] INFO  e.h.j.discord.SAXSequiturDiscord - loaded 2001 points from 2001 lines in anomaly_01.csv
# 16:06:23.223 [main] INFO  e.h.j.discord.SAXSequiturDiscord - Starting discords search with settings: algorithm 3, data "anomaly_01.csv", window 160, PAA 6, alphabet 6, reporting 5 discords.
# 16:06:23.223 [main] INFO  e.h.j.discord.SAXSequiturDiscord - running SAXSequitur algorithm...
# 16:06:23.312 [main] INFO  e.h.j.discord.SAXSequiturDiscord - found 3 intervals not covered by rules: [599-641],[813-920],[1258-1342],
# params: [3, anomaly_01.csv, 160, 6, 6, 5]
# discord #0 "pos,calls,len,rule 599 2333 42 -1", at 599 distance to closest neighbor: 7.459850716707116, info string: "position 599, length 42, NN distance 7.459850716707116, elapsed time: 0h 0m 0s 38ms, distance calls: 2333"
# discord #1 "pos,calls,len,rule 813 2104 107 -2", at 813 distance to closest neighbor: 6.691643588097744, info string: "position 813, length 107, NN distance 6.691643588097744, elapsed time: 0h 0m 0s 12ms, distance calls: 2104"
# discord #2 "pos,calls,len,rule 926 19756 169 25", at 926 distance to closest neighbor: 3.0652136536400096, info string: "position 926, length 169, NN distance 3.0652136536400096, elapsed time: 0h 0m 0s 48ms, distance calls: 19756"
# discord #3 "pos,calls,len,rule 1562 18300 185 35", at 1562 distance to closest neighbor: 2.9011319278104364, info string: "position 1562, length 185, NN distance 2.9011319278104364, elapsed time: 0h 0m 0s 33ms, distance calls: 18300"
# discord #4 "pos,calls,len,rule 641 13666 172 23", at 641 distance to closest neighbor: 2.811106825222956, info string: "position 641, length 172, NN distance 2.811106825222956, elapsed time: 0h 0m 0s 30ms, distance calls: 13666"
# 
# Discords found in 0h 0m 0s 254ms

data=read.csv(file="../anomaly_01.csv",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(lwd=1.1,color="blue1") + theme_classic() +
  ggtitle("Recorded Video dataset") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p  
red_line=df[1099:(1099+135),]
p = p + geom_line(data=red_line,col="red", lwd=1.6)
green_line=df[1280:(1280+250),]
p = p + geom_line(data=green_line,col="green", lwd=1.6)
violet_line=df[1680:(1680+180),]
p = p + geom_line(data=violet_line,col="deeppink", lwd=1.6)
p
#
density=read.csv(file="../coverage.txt",header=F,sep=",")
which(density==0)
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
density_df$value[400:600]=density_df$value[400:600]-1
shade <- rbind(c(0,0), density_df, c(2229,0))
names(shade)<-c("x","y")
p1 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_classic() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Grammar rules density") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p1  

print(arrangeGrob(p,p1,ncol=1))

Cairo(width = 1000, height = 500, 
      file="ECG13.ps", 
      type="ps", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p,p1, ncol=1))
dev.off()

