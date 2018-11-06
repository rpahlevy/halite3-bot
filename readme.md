# Halite III
### v4

## TODO
- [x] Generate ship di awal terlalu lama, karena yg sudah generated masih nunggu di shipyard
- [ ] [HIGH] kadang stuck jika antre melibatkan lebih dari 4 orang
- [ ] How to solve kalau di near end game ship banyak yg hancur?
- [ ] How to solve kalau dikepung???
- [ ] Swap move masih terlalu kaku
- [ ] Cari halite kayaknya gaperlu antre, ini mesti di navigate nya yg problem, karena finding udah OK

## Log

### 5.0
- We ARE MISSING SOMETHING IMPORTANT: does not book STILL, karena itu yg di shipyard kadang nunggu yg lain pergi dulu
- Small update navigasi, diubah jadi diagonal, coba pengaruh baik apa nggak
- Masih blm ada solusi untk dikepung

### 4.2
- Swap move yeah
- Improve queue so the ship wont stuck

### 4.1
- Add booked flag on cell
- Add queue mode for ship

### 4.0
- Add check shipyard: don't spawn ship if surrounded - *stuck*
- Add alternative node, if best node not found
