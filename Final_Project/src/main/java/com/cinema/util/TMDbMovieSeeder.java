package com.cinema.util;

import com.cinema.entity.Movie;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Tool nạp các phim bom tấn hot và phim sắp chiếu tại rạp Việt Nam
 * từ TMDb vào Database PostgreSQL.
 */
public class TMDbMovieSeeder {

    private static final String API_KEY = "a9d66d0def23baf3db25855422237301";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("🎬 NẠP PHIM BOM TẤN HOT VÀ PHIM SẮP CHIẾU TẠI VIỆT NAM TỪ TMDB...");
        System.out.println("==================================================================");

        try {
            EntityManager em = JPAUtil.getEntityManager();
            EntityTransaction trans = em.getTransaction();

            Set<Integer> targetMovieIds = new LinkedHashSet<>();

            // 1. Danh sách ID các phim bom tấn cực hot tại rạp Việt Nam
            int[] blockbusterIds = {
                533535,  // Deadpool & Wolverine
                1022789, // Những Mảnh Ghép Cảm Xúc 2 (Inside Out 2)
                912649,  // Venom: Kèo Cuối (Venom: The Last Dance)
                1241982, // Hành Trình Của Moana 2
                1258626, // Lật Mặt 7: Một Điều Ước
                1275116, // Ngày Xưa Có Một Chuyện Tình
                1312078, // CÁM
                693134,  // Hành Tinh Cát: Phần Hai (Dune 2)
                872585,  // Oppenheimer
                76600,   // Avatar: Dòng Chảy Của Nước
                823464,  // Godzilla x Kong: Đế Chế Mới
                1214484, // Thám Tử Lừng Danh Conan: Ngôi Sao 5 Cánh 1 Triệu Đô
                1147400  // Doraemon: Bản Giao Hưởng Địa Cầu
            };
            for (int id : blockbusterIds) {
                targetMovieIds.add(id);
            }

            // 2. Lấy thêm các phim sắp chiếu tại rạp Việt Nam (Upcoming in Vietnam)
            List<Integer> upcomingIds = fetchUpcomingMovieIds();
            targetMovieIds.addAll(upcomingIds);

            // 3. Lấy thêm các phim trending thịnh hành tuần này
            List<Integer> trendingIds = fetchTrendingMovieIds();
            targetMovieIds.addAll(trendingIds);

            System.out.println("📋 Tổng số ứng viên phim cần nạp: " + targetMovieIds.size());

            int savedCount = 0;
            int skippedCount = 0;

            for (int tmdbId : targetMovieIds) {
                try {
                    Movie movie = fetchMovieDetails(tmdbId);
                    if (movie == null || movie.getTitle() == null || movie.getPosterURL() == null) {
                        skippedCount++;
                        continue;
                    }

                    // Kiểm tra xem phim đã có trong Database chưa
                    Long count = em.createQuery("SELECT COUNT(m) FROM Movie m WHERE LOWER(m.title) = :title", Long.class)
                            .setParameter("title", movie.getTitle().trim().toLowerCase())
                            .getSingleResult();

                    if (count > 0) {
                        System.out.println("⏩ [Đã có trong DB] Bỏ qua: " + movie.getTitle());
                        skippedCount++;
                        continue;
                    }

                    // Lưu phim mới
                    trans.begin();
                    em.persist(movie);
                    trans.commit();

                    savedCount++;
                    System.out.println(String.format("⭐ [%d] ĐÃ LƯU: %s (%d phút | %s | %s | Khởi chiếu: %s)",
                            savedCount, movie.getTitle(), movie.getDuration(), movie.getGenre(), movie.getAgeRating(), movie.getReleaseDate()));

                    Thread.sleep(100);

                } catch (Exception ex) {
                    if (trans.isActive()) trans.rollback();
                    System.err.println("⚠️ Lỗi phim ID " + tmdbId + ": " + ex.getMessage());
                }
            }

            em.close();
            System.out.println("\n==================================================================");
            System.out.println("🎉 HOÀN TẤT NẠP PHIM MỚI!");
            System.out.println("✅ Đã bổ sung thành công: " + savedCount + " phim hot và sắp chiếu vào Database.");
            System.out.println("==================================================================");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JPAUtil.shutdown();
        }
    }

    private static List<Integer> fetchUpcomingMovieIds() {
        List<Integer> list = new ArrayList<>();
        try {
            String url = String.format("%s/movie/upcoming?api_key=%s&language=vi-VN&region=VN&page=1", BASE_URL, API_KEY);
            String json = sendHttpGet(url);
            if (json != null) {
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("results")) {
                    for (JsonElement el : obj.getAsJsonArray("results")) {
                        JsonObject m = el.getAsJsonObject();
                        if (m.has("id")) list.add(m.get("id").getAsInt());
                    }
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static List<Integer> fetchTrendingMovieIds() {
        List<Integer> list = new ArrayList<>();
        try {
            String url = String.format("%s/trending/movie/week?api_key=%s&language=vi-VN", BASE_URL, API_KEY);
            String json = sendHttpGet(url);
            if (json != null) {
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("results")) {
                    for (JsonElement el : obj.getAsJsonArray("results")) {
                        JsonObject m = el.getAsJsonObject();
                        if (m.has("id")) list.add(m.get("id").getAsInt());
                    }
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static Movie fetchMovieDetails(int tmdbId) {
        try {
            String urlVi = String.format("%s/movie/%d?api_key=%s&language=vi-VN&append_to_response=credits", BASE_URL, tmdbId, API_KEY);
            String jsonVi = sendHttpGet(urlVi);
            if (jsonVi == null) return null;

            JsonObject objVi = JsonParser.parseString(jsonVi).getAsJsonObject();

            String title = objVi.has("title") && !objVi.get("title").isJsonNull()
                    ? objVi.get("title").getAsString() : null;
            if (title == null || title.trim().isEmpty()) {
                if (objVi.has("original_title") && !objVi.get("original_title").isJsonNull()) {
                    title = objVi.get("original_title").getAsString();
                }
            }

            String posterPath = objVi.has("poster_path") && !objVi.get("poster_path").isJsonNull()
                    ? objVi.get("poster_path").getAsString() : null;
            if (posterPath == null || posterPath.trim().isEmpty()) {
                return null;
            }
            String posterURL = IMAGE_BASE_URL + posterPath;

            String description = objVi.has("overview") && !objVi.get("overview").isJsonNull()
                    ? objVi.get("overview").getAsString() : "";
            if (description == null || description.trim().isEmpty()) {
                String urlEn = String.format("%s/movie/%d?api_key=%s&language=en-US", BASE_URL, tmdbId, API_KEY);
                String jsonEn = sendHttpGet(urlEn);
                if (jsonEn != null) {
                    JsonObject objEn = JsonParser.parseString(jsonEn).getAsJsonObject();
                    if (objEn.has("overview") && !objEn.get("overview").isJsonNull()) {
                        description = objEn.get("overview").getAsString();
                    }
                }
            }
            if (description == null || description.trim().isEmpty()) {
                description = "Bộ phim chiếu rạp đặc sắc " + title + " với dàn diễn viên xuất sắc cùng những kỹ xảo điện ảnh đỉnh cao.";
            }

            int duration = objVi.has("runtime") && !objVi.get("runtime").isJsonNull()
                    ? objVi.get("runtime").getAsInt() : 0;
            if (duration <= 0) duration = 115;

            LocalDate releaseDate = LocalDate.now();
            if (objVi.has("release_date") && !objVi.get("release_date").isJsonNull()) {
                String rdStr = objVi.get("release_date").getAsString();
                if (rdStr != null && rdStr.length() >= 10) {
                    try {
                        releaseDate = LocalDate.parse(rdStr.substring(0, 10));
                    } catch (Exception ignored) {}
                }
            }

            List<String> genreList = new ArrayList<>();
            if (objVi.has("genres") && objVi.get("genres").isJsonArray()) {
                for (JsonElement g : objVi.getAsJsonArray("genres")) {
                    JsonObject gobj = g.getAsJsonObject();
                    if (gobj.has("name")) {
                        genreList.add(gobj.get("name").getAsString());
                    }
                }
            }
            String genre = genreList.isEmpty() ? "Hành Động, Tâm Lý" : String.join(", ", genreList);
            if (genre.length() > 95) {
                genre = genre.substring(0, 95);
            }

            String director = "Đang cập nhật";
            if (objVi.has("credits") && objVi.get("credits").isJsonObject()) {
                JsonObject credits = objVi.getAsJsonObject("credits");
                if (credits.has("crew") && credits.get("crew").isJsonArray()) {
                    for (JsonElement c : credits.getAsJsonArray("crew")) {
                        JsonObject crewObj = c.getAsJsonObject();
                        if (crewObj.has("job") && "Director".equalsIgnoreCase(crewObj.get("job").getAsString())) {
                            director = crewObj.get("name").getAsString();
                            break;
                        }
                    }
                }
            }

            String ageRating = "T13";
            String genreLower = genre.toLowerCase();
            if (genreLower.contains("hoạt hình") || genreLower.contains("gia đình")) {
                ageRating = "P";
            } else if (genreLower.contains("kinh dị") || genreLower.contains("tội phạm")) {
                ageRating = "T18";
            } else if (genreLower.contains("hành động") || genreLower.contains("chiến tranh") || genreLower.contains("bí ẩn")) {
                ageRating = "T16";
            }

            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setDescription(description);
            movie.setPosterURL(posterURL);
            movie.setDirector(director);
            movie.setDuration(duration);
            movie.setReleaseDate(releaseDate);
            movie.setGenre(genre);
            movie.setAgeRating(ageRating);

            return movie;
        } catch (Exception e) {
            return null;
        }
    }

    private static String sendHttpGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
