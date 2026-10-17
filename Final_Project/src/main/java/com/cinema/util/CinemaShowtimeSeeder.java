package com.cinema.util;

import com.cinema.dao.catalog.CinemaDAO;
import com.cinema.dao.catalog.RoomDAO;
import com.cinema.dao.catalog.SeatDAO;
import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class CinemaShowtimeSeeder {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("🍿 BẮT ĐẦU NẠP DỮ LIỆU CỤM RẠP, PHÒNG CHIẾU, GHẾ & SUẤT CHIẾU...");
        System.out.println("==================================================================");

        try {
            CinemaDAO cinemaDAO = new CinemaDAO();
            RoomDAO roomDAO = new RoomDAO();
            SeatDAO seatDAO = new SeatDAO();
            EntityManager em = JPAUtil.getEntityManager();

            // 1. Kiểm tra hoặc tạo Cụm Rạp
            List<Cinema> existingCinemas = cinemaDAO.findAll();
            if (existingCinemas.isEmpty()) {
                System.out.println("🏛️ Đang tạo 3 cụm rạp chi nhánh chuẩn...");
                Cinema c1 = new Cinema();
                c1.setName("Cinetik Landmark 81");
                c1.setAddress("Tầng B1, TTTM Vincom Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP.HCM");
                cinemaDAO.insert(c1);

                Cinema c2 = new Cinema();
                c2.setName("Cinetik Nguyễn Du");
                c2.setAddress("116 Nguyễn Du, Phường Bến Thành, Quận 1, TP.HCM");
                cinemaDAO.insert(c2);

                Cinema c3 = new Cinema();
                c3.setName("Cinetik Crescent Mall");
                c3.setAddress("Tầng 5, TTTM Crescent Mall, Đại Lộ Nguyễn Văn Linh, Tân Phú, Quận 7, TP.HCM");
                cinemaDAO.insert(c3);

                existingCinemas = cinemaDAO.findAll();
            }
            System.out.println("✅ Đã sẵn sàng " + existingCinemas.size() + " cụm rạp.");

            // 2. Kiểm tra hoặc tạo Phòng Chiếu & Sơ đồ Ghế cho từng rạp
            for (Cinema cinema : existingCinemas) {
                List<Room> rooms = roomDAO.findByCinemaId(cinema.getCinemaID());
                if (rooms.isEmpty()) {
                    System.out.println("🚪 Tạo phòng chiếu cho rạp: " + cinema.getName());

                    Room r1 = new Room();
                    r1.setName("Phòng 01 (Laser 2D)");
                    r1.setCapacity(80);
                    r1.setCinema(cinema);
                    roomDAO.insert(r1);
                    seatDAO.generateRoomSeats(r1.getRoomID(), 8, 10, Arrays.asList("E", "F", "G"), Arrays.asList("H"));

                    Room r2 = new Room();
                    r2.setName("Phòng 02 (IMAX Laser)");
                    r2.setCapacity(100);
                    r2.setCinema(cinema);
                    roomDAO.insert(r2);
                    seatDAO.generateRoomSeats(r2.getRoomID(), 10, 10, Arrays.asList("F", "G", "H"), Arrays.asList("I", "J"));
                }
            }

            // 3. Lấy toàn bộ danh sách phòng chiếu
            List<Room> allRooms = roomDAO.findAll();
            if (allRooms.isEmpty()) {
                System.err.println("❌ Không tìm thấy phòng chiếu nào.");
                return;
            }

            // 4. Lấy danh sách các phim trong Database
            List<Movie> movies = em.createQuery("SELECT m FROM Movie m ORDER BY m.title", Movie.class).setMaxResults(15).getResultList();
            if (movies.isEmpty()) {
                System.err.println("❌ Không tìm thấy phim nào trong Database. Hãy nạp phim trước.");
                return;
            }

            System.out.println("🎬 Tìm thấy " + movies.size() + " phim để lên lịch chiếu...");

            // 5. Kiểm tra số lượng suất chiếu hiện có
            Long countShowtimes = em.createQuery("SELECT COUNT(s) FROM Showtime s", Long.class).getSingleResult();
            if (countShowtimes < 20) {
                System.out.println("📅 Đang tạo suất chiếu tự động cho Hôm Nay và 5 ngày tiếp theo...");

                EntityTransaction trans = em.getTransaction();
                trans.begin();

                LocalTime[] timeSlots = {
                    LocalTime.of(9, 15),
                    LocalTime.of(11, 45),
                    LocalTime.of(14, 30),
                    LocalTime.of(17, 15),
                    LocalTime.of(19, 45),
                    LocalTime.of(21, 30),
                    LocalTime.of(23, 15)
                };

                LocalDate today = LocalDate.now();
                int showtimeCreated = 0;

                for (int dayOffset = 0; dayOffset <= 5; dayOffset++) {
                    LocalDate showDate = today.plusDays(dayOffset);

                    int movieIndex = dayOffset % movies.size();
                    for (int rIdx = 0; rIdx < allRooms.size(); rIdx++) {
                        Room room = allRooms.get(rIdx);

                        // Chọn 3-4 khung giờ cho phòng này
                        for (int tIdx = (rIdx % 2); tIdx < timeSlots.length; tIdx += 2) {
                            Movie movie = movies.get((movieIndex + rIdx + tIdx) % movies.size());
                            LocalTime startTime = timeSlots[tIdx];
                            LocalTime endTime = startTime.plusMinutes(movie.getDuration() > 0 ? movie.getDuration() + 15 : 120);

                            LocalDateTime startDateTime = showDate.atTime(startTime);
                            LocalDateTime endDateTime = showDate.atTime(endTime);

                            Showtime st = new Showtime();
                            st.setMovie(movie);
                            st.setRoom(room);
                            st.setStartTime(startDateTime);
                            st.setEndTime(endDateTime);

                            em.persist(st);
                            showtimeCreated++;
                        }
                    }
                }

                trans.commit();
                System.out.println("✅ Đã tạo thành công " + showtimeCreated + " suất chiếu phân bổ đều cho 6 ngày tới!");
            } else {
                System.out.println("⏩ Đã có sẵn " + countShowtimes + " suất chiếu trong Database.");
            }

            em.close();
            System.out.println("==================================================================");
            System.out.println("🎉 HOÀN TẤT NẠP DỮ LIỆU CỤM RẠP & LỊCH CHIẾU!");
            System.out.println("==================================================================");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JPAUtil.shutdown();
        }
    }
}
