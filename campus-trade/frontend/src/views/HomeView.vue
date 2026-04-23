<template>
  <div class="home-page">
    <div class="hero-section">
      <div class="hero-bg">
        <div class="hero-blob blob-a"></div>
        <div class="hero-blob blob-b"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">发现校园好物</h1>
        <p class="hero-sub">闲置不闲，让每件物品找到新主人</p>
      </div>
    </div>

    <div class="category-bar">
      <div class="category-list">
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['cat-chip', { active: searchForm.categoryId === cat.id }]"
          @click="selectCategory(cat.id)"
        >{{ cat.name }}</button>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索商品..."
        class="filter-input"
        clearable
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
      <el-input-number
        v-model="searchForm.minPrice"
        :min="0" placeholder="最低价"
        class="price-input" controls-position="right"
      />
      <span class="price-sep">—</span>
      <el-input-number
        v-model="searchForm.maxPrice"
        :min="0" placeholder="最高价"
        class="price-input" controls-position="right"
      />
      <el-select v-model="searchForm.sortBy" class="sort-select" @change="doSearch">
        <el-option label="最新发布" value="latest" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="最多浏览" value="views" />
      </el-select>
      <el-button class="search-btn" type="primary" :icon="Search" @click="doSearch">搜索</el-button>
      <el-button class="reset-btn" @click="resetSearch">重置</el-button>
    </div>

    <div v-loading="loading" class="goods-grid">
      <div
        v-for="item in goodsList"
        :key="item.id"
        class="goods-card"
        @click="$router.push(`/goods/${item.id}`)"
      >
        <div class="card-img-wrap">
          <img
            :src="item.coverUrl || '/default-goods.png'"
            class="card-img"
            :alt="item.title"
          />
          <div class="card-overlay">
            <span class="overlay-text">查看详情</span>
          </div>
          <span :class="['status-badge', 'status-' + item.status]">
            {{ statusLabel(item.status) }}
          </span>
        </div>
        <div class="card-body">
          <p class="card-title" :title="item.title">{{ item.title }}</p>
          <div class="card-price-row">
            <span class="card-price">¥{{ item.price }}</span>
            <span v-if="item.originalPrice" class="card-original">¥{{ item.originalPrice }}</span>
          </div>
          <div class="card-meta">
            <span class="meta-location">📍 {{ item.location || '校园内' }}</span>
            <span class="meta-views">👁 {{ item.views }}</span>
          </div>
          <div class="card-seller">
            <el-avatar :size="22" :src="item.sellerAvatar" class="seller-avatar">
              {{ item.sellerName?.[0] || 'U' }}
            </el-avatar>
            <span class="seller-name">{{ item.sellerName }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !goodsList.length" description="暂无商品" class="empty-state" />
    </div>

    <el-pagination
      v-if="total > searchForm.size"
      class="pagination"
      v-model:current-page="currentPage"
      v-model:page-size="searchForm.size"
      :total="total"
      :page-sizes="[12, 24, 48]"
      layout="total, sizes, prev, pager, next"
      @current-change="loadGoods"
      @size-change="doSearch"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'

const route       = useRoute()
const loading     = ref(false)
const goodsList   = ref([])
const categories  = ref([])
const total       = ref(0)
const currentPage = ref(1)

const searchForm = reactive({
  keyword:    '',
  categoryId: null,
  minPrice:   null,
  maxPrice:   null,
  sortBy:     'latest',
  page:       0,
  size:       12,
})

onMounted(async () => {
  await loadCategories()
  if (route.query.keyword) searchForm.keyword = route.query.keyword
  loadGoods()
})

watch(() => route.query.keyword, val => {
  if (val !== undefined) { searchForm.keyword = val; doSearch() }
})

async function loadCategories() {
  const res = await request.get('/categories')
  categories.value = (res.data.data || []).map(c => ({ id: c.id, name: c.name }))
}

async function loadGoods() {
  loading.value = true
  try {
    searchForm.page = currentPage.value - 1
    const params = {}
    if (searchForm.keyword)    params.keyword    = searchForm.keyword
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId
    if (searchForm.minPrice)   params.minPrice   = searchForm.minPrice
    if (searchForm.maxPrice)   params.maxPrice   = searchForm.maxPrice
    params.sortBy = searchForm.sortBy
    params.page   = searchForm.page
    params.size   = searchForm.size
    const res = await request.get('/goods', { params })
    goodsList.value = res.data.data?.list || []
    total.value     = res.data.data?.total || 0
  } finally {
    loading.value = false
  }
}

function doSearch() { currentPage.value = 1; loadGoods() }

function resetSearch() {
  Object.assign(searchForm, { keyword:'', categoryId:null, minPrice:null, maxPrice:null, sortBy:'latest' })
  doSearch()
}

function selectCategory(id) {
  searchForm.categoryId = searchForm.categoryId === id ? null : id
  doSearch()
}

const statusLabel = s => ({ ON_SALE:'在售', RESERVED:'已预订', SOLD:'已售', DRAFT:'草稿' }[s] || s)
</script>

<style scoped>
.home-page { animation: fadeIn 0.5s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.hero-section {
  position: relative;
  border-radius: var(--radius-xl, 20px);
  overflow: hidden;
  padding: 48px 40px;
  margin-bottom: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.hero-bg { position: absolute; inset: 0; overflow: hidden; }
.hero-blob {
  position: absolute; border-radius: 50%; filter: blur(60px); opacity: 0.4;
  animation: heroFloat 12s infinite alternate ease-in-out;
}
.blob-a { width: 300px; height: 300px; background: #e0c3fc; top: -60px; right: -40px; }
.blob-b { width: 250px; height: 250px; background: #8ec5fc; bottom: -40px; left: 20%; animation-delay: -4s; }
@keyframes heroFloat {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30px, -20px) scale(1.1); }
}
.hero-content { position: relative; z-index: 1; }
.hero-title {
  font-size: 32px; font-weight: 800; color: #fff; margin-bottom: 8px;
  letter-spacing: -0.5px;
}
.hero-sub { font-size: 16px; color: rgba(255,255,255,0.85); font-weight: 500; }

.category-bar { margin-bottom: 20px; overflow-x: auto; }
.category-list { display: flex; gap: 10px; padding: 4px 0; }
.cat-chip {
  padding: 8px 20px; border-radius: 20px; border: 1px solid var(--border-light, #e2e8f0);
  background: rgba(255,255,255,0.8); color: var(--text-secondary, #64748b);
  font-size: 14px; font-weight: 500; cursor: pointer; white-space: nowrap;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}
.cat-chip:hover {
  border-color: var(--primary, #667eea); color: var(--primary, #667eea);
  background: rgba(102, 126, 234, 0.06);
}
.cat-chip.active {
  background: var(--gradient-primary, linear-gradient(135deg, #667eea, #764ba2));
  color: #fff; border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.filter-bar {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 24px;
  padding: 16px 20px;
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-lg, 16px);
  border: 1px solid var(--border-light, #e2e8f0);
}
.filter-input { width: 220px; }
.filter-input :deep(.el-input__wrapper) {
  border-radius: 10px; background: rgba(241,245,249,0.8);
  box-shadow: inset 0 0 0 1px var(--border-light, #e2e8f0);
}
.price-input { width: 120px; }
.price-input :deep(.el-input__wrapper) { border-radius: 10px; }
.price-sep { color: var(--text-muted, #94a3b8); font-weight: 500; }
.sort-select { width: 140px; }
.sort-select :deep(.el-input__wrapper) { border-radius: 10px; }
.search-btn {
  border-radius: 10px;
  background: var(--gradient-primary, linear-gradient(135deg, #667eea, #764ba2));
  border: none; font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}
.search-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35); }
.reset-btn { border-radius: 10px; font-weight: 500; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px; min-height: 200px;
}

.goods-card {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-lg, 16px);
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.9);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
  border-color: rgba(102, 126, 234, 0.2);
}

.card-img-wrap {
  position: relative; padding-top: 75%; background: #f1f5f9; overflow: hidden;
}
.card-img {
  position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover;
  transition: transform 0.5s ease;
}
.goods-card:hover .card-img { transform: scale(1.08); }

.card-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.3s ease;
}
.goods-card:hover .card-overlay { opacity: 1; }
.overlay-text {
  color: #fff; font-size: 14px; font-weight: 600;
  padding: 8px 20px; border-radius: 20px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255,255,255,0.3);
}

.status-badge {
  position: absolute; top: 10px; right: 10px;
  padding: 4px 10px; border-radius: 8px;
  font-size: 11px; font-weight: 600;
  backdrop-filter: blur(8px);
}
.status-ON_SALE { background: rgba(16, 185, 129, 0.85); color: #fff; }
.status-RESERVED { background: rgba(245, 158, 11, 0.85); color: #fff; }
.status-SOLD { background: rgba(107, 114, 128, 0.85); color: #fff; }
.status-DRAFT { background: rgba(239, 68, 68, 0.85); color: #fff; }

.card-body { padding: 14px 16px; }
.card-title {
  font-size: 15px; font-weight: 600; color: var(--text-primary, #1e293b);
  overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
  margin-bottom: 8px; line-height: 1.4;
}
.card-price-row { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; }
.card-price {
  font-size: 20px; font-weight: 800;
  background: linear-gradient(135deg, #e53935, #ff6f00);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.card-original { font-size: 12px; color: var(--text-muted, #94a3b8); text-decoration: line-through; }
.card-meta {
  display: flex; justify-content: space-between; font-size: 12px;
  color: var(--text-muted, #94a3b8); margin-bottom: 10px;
}
.card-seller {
  display: flex; align-items: center; gap: 8px;
  padding-top: 10px; border-top: 1px solid rgba(226,232,240,0.6);
}
.seller-avatar { flex-shrink: 0; }
.seller-name {
  font-size: 12px; font-weight: 500; color: var(--text-secondary, #64748b);
  overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
}

.empty-state { grid-column: 1 / -1; padding: 60px 0; }

.pagination {
  margin-top: 32px; justify-content: center;
  padding: 16px;
  background: rgba(255,255,255,0.6);
  border-radius: var(--radius-md, 12px);
}
</style>
