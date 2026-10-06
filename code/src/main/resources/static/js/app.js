// PlantPal — พฤติกรรมฝั่งหน้าเว็บอย่างเดียว (ยังไม่เชื่อม backend)
// ปุ่มที่มี data-open="id" เปิด modal, data-close ปิด modal
document.addEventListener("click", function (e) {
  var open = e.target.closest("[data-open]");
  if (open) {
    e.preventDefault();
    var m = document.getElementById(open.dataset.open);
    if (m) {
      // ส่งค่าจากปุ่มไปเติมในฟอร์ม เช่น data-fill-plant="ม่อนม่อน"
      Object.keys(open.dataset).forEach(function (k) {
        if (k.indexOf("fill") === 0) {
          var name = k.slice(4).toLowerCase();
          var el = m.querySelector('[data-fill="' + name + '"]');
          if (el) { if ("value" in el) el.value = open.dataset[k]; else el.textContent = open.dataset[k]; }
        }
      });
      m.hidden = false;
      var first = m.querySelector("input:not([readonly]), select, textarea, button");
      if (first) first.focus();
    }
    return;
  }
  if (e.target.closest("[data-close]") || e.target.classList.contains("modal-backdrop")) {
    var bd = e.target.closest(".modal-backdrop");
    if (bd) bd.hidden = true;
    return;
  }
  // ติ๊กงานเสร็จ
  var check = e.target.closest(".check");
  if (check) {
    var task = check.closest(".task");
    task.classList.toggle("done");
    check.setAttribute("aria-pressed", task.classList.contains("done"));
    return;
  }
  // chip กรอง: กลุ่มเดียวกันเลือกได้ทีละอัน แล้วซ่อน item ที่ data-tags ไม่ตรง
  var chip = e.target.closest(".chip[data-filter]");
  if (chip) {
    var group = chip.closest(".chips");
    group.querySelectorAll(".chip").forEach(function (c) { c.classList.remove("active"); });
    chip.classList.add("active");
    var target = document.querySelector(group.dataset.target);
    if (target) {
      target.querySelectorAll("[data-tags]").forEach(function (item) {
        var f = chip.dataset.filter;
        item.hidden = !(f === "all" || item.dataset.tags.split(" ").indexOf(f) !== -1);
      });
    }
    return;
  }
  // อ่านแล้ว
  var read = e.target.closest("[data-mark-read]");
  if (read) {
    read.closest(".note").classList.remove("unread");
    read.remove();
  }
});

document.addEventListener("keydown", function (e) {
  if (e.key === "Escape") document.querySelectorAll(".modal-backdrop").forEach(function (m) { m.hidden = true; });
});

// ช่องค้นหาต้นไม้: ค้นจากชื่อเล่น + ชื่อพันธุ์
var search = document.getElementById("plant-search");
if (search) {
  search.addEventListener("input", function () {
    var q = search.value.trim().toLowerCase();
    document.querySelectorAll("#plant-grid [data-search]").forEach(function (card) {
      card.hidden = q !== "" && card.dataset.search.toLowerCase().indexOf(q) === -1;
    });
  });
}

// ล็อกอิน (หน้าตัวอย่าง): แยกหน้าปลายทางตาม role
// ของจริง Spring Security ตรวจ users.role แล้ว redirect ให้ ไม่ใช้โค้ดส่วนนี้
var loginForm = document.getElementById("login-form");
if (loginForm) {
  loginForm.addEventListener("submit", function (e) {
    e.preventDefault();
    var email = loginForm.querySelector('[name="email"]').value.trim().toLowerCase();
    var isAdmin = email === "admin@plantpal.com";
    window.location.href = isAdmin ? "admin-reports.html" : "dashboard.html";
  });
}
var registerForm = document.getElementById("register-form");
if (registerForm) {
  registerForm.addEventListener("submit", function (e) {
    e.preventDefault();
    window.location.href = "login.html";   // สมัครเสร็จ กลับไปล็อกอิน (role = USER เสมอ)
  });
}

// ฟอร์มอื่นยังไม่ส่งไปไหน (รอเชื่อม controller)
// ฟอร์มที่ต่อ backend แล้ว ให้ใส่ data-live ในแท็ก <form> จะส่งข้อมูลไป controller ได้จริง
document.querySelectorAll("form:not(#login-form):not(#register-form):not([data-live])").forEach(function (f) {
  f.addEventListener("submit", function (e) {
    e.preventDefault();
    var bd = f.closest(".modal-backdrop");
    if (bd) bd.hidden = true;
  });
});

// ตอนโหลดหน้า: ใช้ตัวกรองที่ถูกเลือกไว้ตั้งแต่แรก (เช่น หน้าสุขภาพเริ่มที่ "ต้องดูแลเป็นพิเศษ")
document.querySelectorAll(".chips[data-target] .chip.active[data-filter]").forEach(function (c) {
  if (c.dataset.filter !== "all") c.click();
});