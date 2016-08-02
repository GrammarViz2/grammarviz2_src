library(ggplot2)
#
# smooth, normal sine
#
t <- seq(0, pi*60, 0.5)
y <- sin(t)
p <- ggplot(data.frame(x = t, y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave")
p
#
# adding random noise
#
y <- sin(t) + 0.1 * (rnorm(length(t)) - 0.5)
p1 <- ggplot(data.frame(x = t, y = y), aes(x, y)) + geom_line() + ggtitle("Sine wave + random noise")
p1
