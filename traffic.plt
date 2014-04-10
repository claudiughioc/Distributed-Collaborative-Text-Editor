set boxwidth 0.2
set style fill solid
set xrange [0:12]
set yrange [0:20000]
set terminal png size 1000,700 enhanced font "Helvetica,10"
set output 'traffic.png'
plot "traffic.dat" using 1:3:xtic(2) with boxes
