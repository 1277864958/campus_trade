<template>
  <div class="home-page">
    <!-- 分类导航 -->
    <div class="category-bar">
      <el-scrollbar>
        <div class="category-list">
          <el-tag
            v-for="cat in categories"
            :key="cat.id"
            :type="searchForm.categoryId === cat.id ? '' : 'info'"
            class="cat-tag"
            @click="selectCategory(cat.id)"
            effect="plain"
          >{{ cat.name }}</el-tag>
        </div>
      </el-scrollbar>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索商品..."
        style="width:240px"
        clearable
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
      <el-input-number
        v-model="searchForm.minPrice"
        :min="0" placeholder="最低价"
        style="width:120px" controls-position="right"
      />
      <span>—</span>
      <el-input-number
        v-model="searchForm.maxPrice"
        :min="0" placeholder="最高价"
        style="width:120px" controls-position="right"
      />
      <el-select v-model="searchForm.sortBy" style="width:130px" @change="doSearch">
        <el-option label="最新发布" value="latest" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="最多浏览" value="views" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="doSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 商品网格 -->
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
          <el-tag class="status-tag" size="small" :type="statusType(item.status)">
            {{ statusLabel(item.status) }}
          </el-tag>
        </div>
        <div class="card-body">
          <p class="card-title" :title="item.title">{{ item.title }}</p>
          <div class="card-price">
            <span class="price">¥{{ item.price }}</span>
            <span v-if="item.originalPrice" class="original-price">¥{{ item.originalPrice }}</span>
          </div>
          <div class="card-meta">
            <span class="location">📍 {{ item.location || '校园内' }}</span>
            <span class="views">👁 {{ item.views }}</span>
          </div>
          <div class="seller-info">
            <el-avatar :size="20" :src="item.sellerAvatar">
              {{ item.sellerName?.[0] || 'U' }}
            </el-avatar>
            <span class="seller-name">{{ item.sellerName }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && !goodsList.length" description="暂无商品" />
    </div>

    <!-- 分页 -->
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
  const res = await request.get('/api/categories')
  // 只取一级分类展示
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
    const res = await request.get('/api/goods', { params })
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

const statusType  = s => ({ ON_SALE:'success', RESERVED:'warning', SOLD:'info', DRAFT:'danger' }[s] || '')
const statusLabel = s => ({ ON_SALE:'在售', RESERVED:'已预订', SOLD:'已售', DRAFT:'草稿' }[s] || s)
</script>

<style scoped>
.category-bar { margin-bottom: 16px; }
.category-list { display:flex; gap:8px; padding:4px 0; white-space:nowrap; }
.cat-tag { cursor:pointer; }

.filter-bar { display:flex; align-items:center; gap:10px; flex-wrap:wrap; margin-bottom:20px; }

.goods-grid {
  display:grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap:16px; min-height:200px;
}
.goods-card {
  background:#fff; border-radius:12px; overflow:hidden;
  box-shadow:0 2px 8px rgba(0,0,0,.06); cursor:pointer;
  transition:transform .2s, box-shadow .2s;
}
.goods-card:hover { transform:translateY(-4px); box-shadow:0 6px 20px rgba(0,0,0,.12); }

.card-img-wrap { position:relative; padding-top:75%; background:#f5f5f5; }
.card-img { position:absolute; inset:0; width:100%; height:100%; object-fit:cover; }
.status-tag { position:absolute; top:8px; right:8px; }

.card-body { padding:12px; }
.card-title { font-size:14px; font-weight:500; overflow:hidden; white-space:nowrap; text-overflow:ellipsis; margin-bottom:6px; }
.card-price { display:flex; align-items:baseline; gap:6px; margin-bottom:6px; }
.price { font-size:18px; font-weight:700; color:#e53935; }
.original-price { font-size:12px; color:#bbb; text-decoration:line-through; }
.card-meta { display:flex; justify-content:space-between; font-size:12px; color:#999; margin-bottom:8px; }
.seller-info { display:flex; align-items:center; gap:6px; font-size:12px; color:#666; }
.seller-name { overflow:hidden; white-space:nowrap; text-overflow:ellipsis; max-width:120px; }

.pagination { margin-top:32px; justify-content:center; }
</style>
